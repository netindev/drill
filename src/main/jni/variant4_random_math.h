#ifndef VARIANT4_RANDOM_MATH_H
#define VARIANT4_RANDOM_MATH_H

// Register size can be configured to either 32 or 64 bit
#if RANDOM_MATH_64_BIT == 1
typedef uint64_t v4_reg;
#else
typedef uint32_t v4_reg;
#endif

enum V4_Settings
{
	// Generate code with latency = 54 cycles, which is equivalent to 18 multiplications
	TOTAL_LATENCY = 18 * 3,
	
	// Available ALUs for MUL
	// Modern CPUs typically have only 1 ALU which can do multiplications
	ALU_COUNT_MUL = 1,

	// Total available ALUs
	// Modern CPUs have 3-4 ALUs, but we use only 2 because random math executes together with other main loop code
	ALU_COUNT = 2,
};

enum V4_InstructionList
{
	// MUL has 3 times higher frequency
	MUL1,	// a*b
	MUL2,	// a*b
	MUL3,	// a*b
	ADD,	// a+b + C, -128 <= C <= 127
	SUB,	// a-b
	ROR,	// rotate right "a" by "b & 31" bits
	ROL,	// rotate left "a" by "b & 31" bits
	XOR,	// a^b
};

// There are 8 registers in total:
// - 4 variable registers
// - 4 constant registers initialized from loop variables
//
// This is why dst_index is 2 bits
struct V4_Instruction
{
	uint8_t opcode : 3;
	uint8_t dst_index : 2;
	uint8_t src_index : 3;
};

static inline void v4_random_math(const struct V4_Instruction* code, int code_size, v4_reg* r)
{
	enum
	{
		REG_BITS = sizeof(v4_reg) * 8,
	};

	for (int i = 0; i < code_size; ++i)
	{
		struct V4_Instruction op = code[i];
		const v4_reg dst = r[op.dst_index];
		const v4_reg src = r[op.src_index];
		uint32_t shift;

		switch (op.opcode)
		{
		case MUL1:
		case MUL2:
		case MUL3:
			r[op.dst_index] = dst * src;
			break;

		case ADD:
			// 3-way addition: a = a + b + C where C is next code byte (signed)
			r[op.dst_index] = dst + src + ((const int8_t*)code)[++i];
			break;

		case SUB:
			r[op.dst_index] = dst - src;
			break;

		case ROR:
			shift = src % REG_BITS;
			r[op.dst_index] = (dst >> shift) | (dst << (REG_BITS - shift));
			break;

		case ROL:
			shift = src % REG_BITS;
			r[op.dst_index] = (dst << shift) | (dst >> (REG_BITS - shift));
			break;

		case XOR:
			r[op.dst_index] = dst ^ src;
			break;
		}
	}
}

// Generates as many random math operations as possible with given latency and ALU restrictions
static inline int v4_random_math_init(struct V4_Instruction* code, const uint64_t height)
{
	// MUL is 3 cycles, all other operations are 1 cycle
	const int op_latency[8] = { 3, 3, 3, 1, 1, 1, 1, 1 };

	// Available ALUs for each instruction
	const int op_ALUs[8] = { ALU_COUNT_MUL, ALU_COUNT_MUL, ALU_COUNT_MUL, ALU_COUNT, ALU_COUNT, ALU_COUNT, ALU_COUNT, ALU_COUNT };

	char data[32];
	memset(data, 0, sizeof(data));
	*((uint64_t*)data) = height;

	int data_index = sizeof(data);

	int latency[8];
	bool alu_busy[TOTAL_LATENCY][ALU_COUNT];

	memset(latency, 0, sizeof(latency));
	memset(alu_busy, 0, sizeof(alu_busy));

	int num_retries = 0;
	int code_size = 0;

	while (((latency[0] < TOTAL_LATENCY) || (latency[1] < TOTAL_LATENCY) || (latency[2] < TOTAL_LATENCY) || (latency[3] < TOTAL_LATENCY)) && (num_retries < 64))
	{
		// If we don't have data available, generate more
		if (data_index >= sizeof(data))
		{
			hash_extra_blake(data, sizeof(data), data);
			data_index = 0;
		}
		struct V4_Instruction op = ((struct V4_Instruction*)data)[data_index++];

		int a = op.dst_index;
		int b = op.src_index;

		// Make sure we don't do SUB/XOR with the same register
		if (((op.opcode == SUB) || (op.opcode == XOR)) && (a == b))
		{
			// a is always < 4, so we don't need to check bounds here
			b = a + 4;
			op.src_index = b;
		}

		// Find which ALU is available (and when) for this instruction
		int next_latency = (latency[a] > latency[b]) ? latency[a] : latency[b];
		int alu_index = -1;
		while ((next_latency < TOTAL_LATENCY) && (alu_index < 0))
		{
			for (int i = op_ALUs[op.opcode] - 1; i >= 0; --i)
			{
				if (!alu_busy[next_latency][i])
				{
					alu_index = i;
					break;
				}
			}
			++next_latency;
		}
		next_latency += op_latency[op.opcode];

		if (next_latency <= TOTAL_LATENCY)
		{
			alu_busy[next_latency - op_latency[op.opcode]][alu_index] = true;
			latency[a] = next_latency;
			code[code_size++] = op;

			// ADD instruction is 2 bytes long. Second byte is a signed constant "C" in "a = a + b + C"
			if (op.opcode == ADD)
			{
				// If we don't have data available, generate more
				if (data_index >= sizeof(data))
				{
					hash_extra_blake(data, sizeof(data), data);
					data_index = 0;
				}
				code[code_size++] = ((struct V4_Instruction*)data)[data_index++];
			}
		}
		else
		{
			++num_retries;
		}
	}

	return code_size;
}

#endif
