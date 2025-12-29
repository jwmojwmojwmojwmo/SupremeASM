# SupremeASM

A assembly/machine code like language made by jwmo in Java for learning purposes and also why not? Also I am bad at naming things so I just picked the first word I thought of.


Development in progress.


## Language Rules

Instructions are 4/8 bytes and written in hex. However, print instructions will print in base 10.


 Anything in memory is stored as ints (4 bytes). One memory slot is equal to 4 bytes. Memory is allocated by the compiler implicitly for instructions. Memory should be explicitly allocated by instructions to store any data, and deallocated if needed. (However, there is nothing stopping one from hardcoding a memory address to use without allocating it)
 
 
 Memory is allocated as blocks, and can only be deallocated block by block. 


Like Java, ints are signed values. Most arithmetic will follow Java rules too (for example, bitshifting by v is actually going to bitshift by v % 32).


Machine code: compiler will remove all whitespace when compiling, so any amount of spaces are allowed. Immediate values are given in hex.


ASM code: compiler will remove all whitespace in between instructions when compiling. Each instruction must be separated by ";", ex: ld #1, 1; ld #2, 0; Immediate values are given in base 10.


## Register Rules


The CPU has ten registers, from r0 - r9, to use. One inaccessible register, PC, serves as the program counter.


PC holds the address of the next instruction to execute, not the current instruction being executed.


r0 recieves return values for all instructions. Most instructions return either 1 for success or -1 for fail. Special cases:
- memory allocation instruction returns address of first memory slot in the allocated memory block
 - memory defragment instruction returns number of blocks defragmented (coalesced together)
 - get user input instruction returns the user's input
- halt returns 0, which tells the CPU to end execution

Note this means that if you want to use the returned value of an instruction, you must move it to another register, as it will be overwritten in r0 once the next instruction completes.

## ISA:

| operation                                                                                                                                                                                                                 | semantics                                           | machine code                      | ASM code                 |
|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------|-----------------------------------|--------------------------|
| load int v into register r                                                                                                                                                                                                | v -> r[r]                                           | 0ree----vvvvvvvv                  | ld #v, r                 |
| load base + offset (o is a signed two byte value)                                                                                                                                                                         | m[r[r] + o] -> r[s]                                 | 00rsoooo                          | ld r+#o, s               |
| load indexed                                                                                                                                                                                                              | m[r[r] + r[o]] -> r[s]                              | 01ros---                          | ld r+o, s                |        
| store base + offset (o is a signed two byte value)                                                                                                                                                                        | r[r] -> m[r[s] + o]                                 | 10rsoooo                          | st r, s+#o               |
| store indexed                                                                                                                                                                                                             | r[r] -> m[r[s] + r[o]]                              | 11rso---                          | st r, s+o                |
| copy register value into another register                                                                                                                                                                                 | r[r] -> r[s]                                        | 20rs----                          | mov r, s                 |
| increment a register value                                                                                                                                                                                                | r[r] + 1 -> r[r]                                    | 210r----                          | inc r                    |
| decrement a register value                                                                                                                                                                                                | r[r] - 1 -> r[r]                                    | 220r----                          | dec r                    |
| add two register values                                                                                                                                                                                                   | r[r] + r[s] -> r[s]                                 | 23rs----                          | add r, s                 |
| subtract two register values                                                                                                                                                                                              | r[r] - r[s] -> r[s]                                 | N/A -> ONLY AVAILABLE IN ASM CODE | sub r, s                 |
| not a register value                                                                                                                                                                                                      | ~r[r] -> r[r]                                       | 240r----                          | not r                    |
| and two register values                                                                                                                                                                                                   | r[r] & r[s] -> r[s]                                 | 25rs----                          | and r, s                 |
| bitshift a register value (bitshift right if v < 0, bitshift left otherwise. vv is a signed 1 byte value)                                                                                                                 | if v < 0: r[r] >> v -> r[r] else: r[r] << v -> r[r] | 26r---vv                          | shf #v, r                |
| multiply two register values                                                                                                                                                                                              | r[r] * r[s] -> r[s]                                 | 27rs----                          | mul r, s                 |
| divide two register values, with the result being truncated to zero                                                                                                                                                       | r[r] / r[s] -> r[s]                                 | 28rs----                          | div r, s                 |
| modulus two register values                                                                                                                                                                                               | r[r] % r[s] -> r[s]                                 | 29rs----                          | mod r, s                 |
| indirect jump to another instruction (o is a signed 2 byte value)                                                                                                                                                         | pc + o -> pc                                        | a00-oooo                          | jmp #o                   |
| if a register value is equal to zero, then indirect jump to another instruction (o is a signed 2 byte value)                                                                                                              | if r[r] == 0: pc + o -> pc                          | a1r-oooo                          | ife r, #o                |
| if a register value is greater than another, then indirect jump to another instruction (o is a signed 2 byte value)                                                                                                       | if r[r] > r[s]: pc + o -> pc                        | a2rsoooo                          | igt r, s, #o             |
| load the value of pc plus an offset to a register (o is a signed two byte value)                                                                                                                                          | pc + o -> r[r]                                      | a3r-oooo                          | gpc r, #o                |
| direct jump to another instruction                                                                                                                                                                                        | v -> pc                                             | afee----vvvvvvvv                  | goto #v                  |
| print register value in terminal                                                                                                                                                                                          | print(r[r])                                         | e00r----                          | DEPRECATED! USE prt r+#0 |
| print memory value with base + offset in terminal                                                                                                                                                                         | print(m[r[r] + o])                                  | e1ro----                          | prt r+#o                 |
| print indexed memory value                                                                                                                                                                                                | print(m[r[r] + r[o])                                | e2ro----                          | prt r+o                  |
| print register value in terminal with ascii conversion                                                                                                                                                                    | printWithFormatting(r[r])                           | e30r----                          | DEPRECATED! USE prf r+#0 |
| print memory value with base + offset in terminal with ascii conversion                                                                                                                                                   | printWithFormatting(m[r[r] + o])                    | e4r-oooo                          | prf r+o                  |
| print indexed memory value with ascii conversion                                                                                                                                                                          | printWithFormatting (m[r[r]+r[o]])                  | e5ro----                          | prf r+#o                 | 
| allocate a memory block with x memory slots (x*4 bytes)                                                                                                                                                                   | malloc(x*4)                                         | f1ee----xxxxxxxx                  | moc #x                   |                
| deallocate the memory block with address stored in register                                                                                                                                                               | free(r[r])                                          | f20r----                          | doc r                    |
| defragment memory (tries to coalesce all memory blocks by iterating over the entire memory until all possible blocks are coalesced. this may take a while if memory is too fragmented)                                    | defrag()                                            | f3------                          | dfg                      |
| get user input. input is parsed as a base 10 int, or a base 16 int if prefixed with "0x". If both of these parsing methods fail, it will take the first character of the input and parse it into its ASCII character code | getInput()                                          | f4------                          | inp                      |
| do nothing                                                                                                                                                                                                                | nop                                                 | f0------                          | nop                      |
| print all register values in terminal                                                                                                                                                                                     | dumpCPU()                                           | fd------                          | dpc                      |   
| print all non-zero memory values in terminal                                                                                                                                                                              | dumpMem()                                           | fe------                          | dpm                      |
| halt                                                                                                                                                                                                                      | halt                                                | ffffffff                          | halt                     |


## Examples:

### Print "Hello World!":
01eeffff00000048 02eeffff00000065 03eeffff0000006c 04eeffff0000006f 05eeffff00000020 06eeffff00000057 07eeffff00000072 08eeffff00000064 09eeffff00000021 e301ffff e302ffff e303ffff e303ffff e304ffff e305ffff e306ffff e304ffff e307ffff e303ffff e308ffff e309ffff ffffffff

### Generate, print, and sum numbers 1-10 using a loop:
01eeffff00000001 02eeffff0000000b 09eeffff0000000a 2013ffff 2403ffff 2103ffff 2323ffff a13f0005 e001ffff e309ffff 2314ffff 2101ffff a00ffff6 e004ffff ffffffff

### Produce the nth Fibonacci Number:
09eeffff0000006e 08eeffff00000020 01eeffff00000000 02eeffff00000001 e309ffff e308ffff f4ffffff 2008ffff a2280007 2208ffff a18f0007 2023ffff 2312ffff 2031ffff 2208ffff a000fffa e001ffff ffffffff e002ffff ffffffff
