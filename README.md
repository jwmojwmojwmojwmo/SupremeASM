# SupremeASM

A assembly/machine code like language made by jwmo in Java for learning purposes and also why not? Also I am bad at naming things so I just picked the first word I thought of.


Development in progress.


## Language Rules

Instructions are 4/8 bytes and written in hex. However, print instructions will print in base 10.


 Anything in memory is stored as ints (4 bytes). One memory slot is equal to 4 bytes. Memory is allocated by the compiler implicitly for instructions. Memory should be explicitly allocated by instructions to store any data, and deallocated if needed. (However, there is nothing stopping one from hardcoding a memory address to use without allocating it)
 
 
 Memory is allocated as blocks, and can only be deallocated block by block. 


Like Java, ints are signed values.


PC holds the next instruction to execute, not the current instruction being executed.


r0 recieves return values for all instructions. Most instructions return either 1 for success or -1 for fail. Special cases:
- memory allocation instruction returns address of first memory slot in the allocated memory block
 - memory defragment instruction returns number of blocks defragmented (coalesced together)
 - get user input instruction returns the user's input
- halt returns 0, which tells the CPU to end execution

Note this means that if you want to use the returned value of an instruction, you must move it to another register, as it will be overwritten in r0 once the next instruction completes.

## ISA:

| operation                                                                                                                                                                                                                 | semantics                                           | machine code     |
|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------|------------------|
| load int v into register r                                                                                                                                                                                                | v -> r[r]                                           | 0ree----vvvvvvvv |
| load base + offset (o is a signed two byte value)                                                                                                                                                                         | m[r[r] + o] -> r[s]                                 | 00rsoooo         |
| load indexed                                                                                                                                                                                                              | m[r[r] + r[o]] -> r[s]                              | 01ros---         |
| store base + offset (o is a signed two byte value)                                                                                                                                                                        | r[r] -> m[r[s] + o]                                 | 10rsoooo         |
| store indexed                                                                                                                                                                                                             | r[r] -> m[r[s] + r[o]]                              | 11rso---         |
| copy register value into another register                                                                                                                                                                                 | r[r] -> r[s]                                        | 20rs----         |
| increment a register value                                                                                                                                                                                                | r[r] + 1 -> r[r]                                    | 210r----         |
| decrement a register value                                                                                                                                                                                                | r[r] - 1 -> r[r]                                    | 220r----         |
| add two register values                                                                                                                                                                                                   | r[r] + r[s] -> r[s]                                 | 23rs----         |
| not a register value                                                                                                                                                                                                      | ~r[r] -> r[r]                                       | 240r----         |
| and two register values                                                                                                                                                                                                   | r[r] & r[s] -> r[s]                                 | 25rs----         |
| bitshift a register value (bitshift right if v < 0, bitshift left otherwise. Bitshifting follows Java rules, so the bitshift is actually v % 32) (vv is a signed 1 byte value)                                            | if v < 0: r[r] >> v -> r[r] else: r[r] << v -> r[r] | 26r---vv         |
| multiply two register values                                                                                                                                                                                              | r[r] * r[s] -> r[s]                                 | 27rs----         |
| divide two register values, with the result being truncated to zero                                                                                                                                                       | r[r] / r[s] -> r[s]                                 | 28rs----         |
| modulus two register values                                                                                                                                                                                               | r[r] % r[s] -> r[s]                                 | 29rs----         |
| indirect jump to another instruction (o is a signed 2 byte value)                                                                                                                                                         | pc + o -> pc                                        | a00-oooo         |
| if a register value is equal to zero, then indirect jump to another instruction (o is a signed 2 byte value)                                                                                                              | if r[r] == 0: pc + o -> pc                          | a1r-oooo         |
| if a register value is greater than another, then indirect jump to another instruction (o is a signed 2 byte value)                                                                                                       | if r[r] > r[s]: pc + o -> pc                        | a2rsoooo         |
| direct jump to another instruction                                                                                                                                                                                        | v -> pc                                             | afee----vvvvvvvv |
| print register value in terminal                                                                                                                                                                                          | print(r[r])                                         | e00r----         |
| print memory value with base + offset in terminal                                                                                                                                                                         | print(m[r[r] + o])                                  | e1ro----         |
| print register value in terminal with ascii conversion                                                                                                                                                                    | printWithFormatting(r[r])                           | e20r----         |
| print memory value with base + offset in terminal with ascii conversion                                                                                                                                                   | printWithFormatting(m[r[r] + o])                    | e3ro----         |
| allocate a memory block with x memory slots (x*4 bytes)                                                                                                                                                                   | malloc(x*4)                                         | f1ee----xxxxxxxx |
| deallocate the memory block with address stored in register                                                                                                                                                               | free(r[r])                                          | f20r----         | 
| defragment memory (tries to coalesce all memory blocks by iterating over the entire memory until all possible blocks are coalesced. this may take a while if memory is too fragmented)                                    | defrag()                                            | f3------         |
| get user input. input is parsed as a base 10 int, or a base 16 int if prefixed with "0x". If both of these parsing methods fail, it will take the first character of the input and parse it into its ASCII character code | getInput()                                          | f4------         |
| do nothing                                                                                                                                                                                                                | nop                                                 | f0------         |
| print all register values in terminal                                                                                                                                                                                     | dumpCPU()                                           | fd------         |    
| print all non-zero memory values in terminal                                                                                                                                                                              | dumpMem()                                           | fe------         |
| halt                                                                                                                                                                                                                      | halt                                                | ffffffff         |


## Examples:

### Print "Hello World!":
01eeffff00000048 02eeffff00000065 03eeffff0000006c 04eeffff0000006f 05eeffff00000020 06eeffff00000057 07eeffff00000072 08eeffff00000064 09eeffff00000021 e201ffff e202ffff e203ffff e203ffff e204ffff e205ffff e206ffff e204ffff e207ffff e203ffff e208ffff e209ffff ffffffff


### Generate, print, and sum numbers 1-10 using a loop:
01eeffff00000001 04eeffff0000000a 02eeffff0000000b 2402ffff 2102ffff 2312ffff a12f0005 e001ffff e104ffff 2313ffff 2101ffff a00ffff5 e003ffff ffffffff

### Produce the nth Fibonacci Number:
09eeffff0000006e 08eeffff00000020 01eeffff00000000 02eeffff00000001 e109ffff e108ffff f4ffffff 2008ffff a2280007 2208ffff a18f0007 2023ffff 2312ffff 2031ffff 2208ffff a000fffa e001ffff ffffffff e002ffff ffffffff