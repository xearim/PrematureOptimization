// All methods.
	.globl main
main:
enter $24, $0
movq $0, -8(%rbp)
movq $0, -16(%rbp)
movq $0, -24(%rbp)
push $10
push $20
pop %r10
pop %r11
addq %r10, %r11
push %r11
pop %r11
movq %r11, -8(%rbp)
movq -8(%rbp), %r11
push %r11
pop %rsi
push .s_0
pop %rdi
movq $0, %rax
call printf
push %rax
addq $8, %rsp
push $10
push $20
pop %r10
pop %r11
sub %r10, %r11
push %r11
pop %r11
movq %r11, -8(%rbp)
movq -8(%rbp), %r11
push %r11
pop %rsi
push .s_1
pop %rdi
movq $0, %rax
call printf
push %rax
addq $8, %rsp
push $10
push $20
pop %r10
pop %r11
imul %r10, %r11
push %r11
pop %r11
movq %r11, -8(%rbp)
movq -8(%rbp), %r11
push %r11
pop %rsi
push .s_2
pop %rdi
movq $0, %rax
call printf
push %rax
addq $8, %rsp
push $1
pop %r11
movq %r11, -8(%rbp)
push $2
pop %r11
movq %r11, -16(%rbp)
push $2
pop %r11
movq %r11, -24(%rbp)
movq -8(%rbp), %r11
push %r11
movq -16(%rbp), %r11
push %r11
pop %r10
pop %r11
cmp %r11, %r10
movq $0, %r11
movq $1, %r10
cmovl %r10, %r11
movq %r11, %r11
push %r11
pop %r10
movq $1, %r11
cmp %r10, %r11
jne .cf_elseif0
push .s_3
pop %rdi
movq $0, %rax
call printf
push %rax
addq $8, %rsp
.cf_endif0:
movq -8(%rbp), %r11
push %r11
movq -16(%rbp), %r11
push %r11
pop %r10
pop %r11
cmp %r11, %r10
movq $0, %r11
movq $1, %r10
cmovle %r10, %r11
movq %r11, %r11
push %r11
pop %r10
movq $1, %r11
cmp %r10, %r11
jne .cf_elseif1
push .s_4
pop %rdi
movq $0, %rax
call printf
push %rax
addq $8, %rsp
.cf_endif1:
movq -8(%rbp), %r11
push %r11
movq -16(%rbp), %r11
push %r11
pop %r10
pop %r11
cmp %r11, %r10
movq $0, %r11
movq $1, %r10
cmovg %r10, %r11
movq %r11, %r11
push %r11
pop %r10
movq $1, %r11
cmp %r10, %r11
jne .cf_elseif2
push .s_5
pop %rdi
movq $0, %rax
call printf
push %rax
addq $8, %rsp
.cf_endif2:
movq -8(%rbp), %r11
push %r11
movq -16(%rbp), %r11
push %r11
pop %r10
pop %r11
cmp %r11, %r10
movq $0, %r11
movq $1, %r10
cmovge %r10, %r11
movq %r11, %r11
push %r11
pop %r10
movq $1, %r11
cmp %r10, %r11
jne .cf_elseif3
push .s_6
pop %rdi
movq $0, %rax
call printf
push %rax
addq $8, %rsp
.cf_endif3:
movq -24(%rbp), %r11
push %r11
movq -16(%rbp), %r11
push %r11
pop %r10
pop %r11
cmp %r11, %r10
movq $0, %r11
movq $1, %r10
cmovl %r10, %r11
movq %r11, %r11
push %r11
pop %r10
movq $1, %r11
cmp %r10, %r11
jne .cf_elseif4
push .s_7
pop %rdi
movq $0, %rax
call printf
push %rax
addq $8, %rsp
.cf_endif4:
movq -24(%rbp), %r11
push %r11
movq -16(%rbp), %r11
push %r11
pop %r10
pop %r11
cmp %r11, %r10
movq $0, %r11
movq $1, %r10
cmovle %r10, %r11
movq %r11, %r11
push %r11
pop %r10
movq $1, %r11
cmp %r10, %r11
jne .cf_elseif5
push .s_8
pop %rdi
movq $0, %rax
call printf
push %rax
addq $8, %rsp
.cf_endif5:
movq -24(%rbp), %r11
push %r11
movq -16(%rbp), %r11
push %r11
pop %r10
pop %r11
cmp %r11, %r10
movq $0, %r11
movq $1, %r10
cmovg %r10, %r11
movq %r11, %r11
push %r11
pop %r10
movq $1, %r11
cmp %r10, %r11
jne .cf_elseif6
push .s_9
pop %rdi
movq $0, %rax
call printf
push %rax
addq $8, %rsp
.cf_endif6:
movq -24(%rbp), %r11
push %r11
movq -16(%rbp), %r11
push %r11
pop %r10
pop %r11
cmp %r11, %r10
movq $0, %r11
movq $1, %r10
cmovge %r10, %r11
movq %r11, %r11
push %r11
pop %r10
movq $1, %r11
cmp %r10, %r11
jne .cf_elseif7
push .s_10
pop %rdi
movq $0, %rax
call printf
push %rax
addq $8, %rsp
.cf_endif7:
movq -8(%rbp), %r11
push %r11
movq -8(%rbp), %r11
push %r11
pop %r10
pop %r11
cmp %r11, %r10
movq $0, %r11
movq $1, %r10
cmove %r10, %r11
movq %r11, %r11
push %r11
pop %r10
movq $1, %r11
cmp %r10, %r11
jne .cf_elseif8
push .s_11
pop %rdi
movq $0, %rax
call printf
push %rax
addq $8, %rsp
.cf_endif8:
movq -8(%rbp), %r11
push %r11
movq -8(%rbp), %r11
push %r11
pop %r10
pop %r11
cmp %r11, %r10
movq $0, %r11
movq $1, %r10
cmovne %r10, %r11
movq %r11, %r11
push %r11
pop %r10
movq $1, %r11
cmp %r10, %r11
jne .cf_elseif9
push .s_12
pop %rdi
movq $0, %rax
call printf
push %rax
addq $8, %rsp
.cf_endif9:
movq -8(%rbp), %r11
push %r11
movq -16(%rbp), %r11
push %r11
pop %r10
pop %r11
cmp %r11, %r10
movq $0, %r11
movq $1, %r10
cmove %r10, %r11
movq %r11, %r11
push %r11
pop %r10
movq $1, %r11
cmp %r10, %r11
jne .cf_elseif10
push .s_13
pop %rdi
movq $0, %rax
call printf
push %rax
addq $8, %rsp
.cf_endif10:
movq -8(%rbp), %r11
push %r11
movq -16(%rbp), %r11
push %r11
pop %r10
pop %r11
cmp %r11, %r10
movq $0, %r11
movq $1, %r10
cmovne %r10, %r11
movq %r11, %r11
push %r11
pop %r10
movq $1, %r11
cmp %r10, %r11
jne .cf_elseif11
push .s_14
pop %rdi
movq $0, %rax
call printf
push %rax
addq $8, %rsp
.cf_endif11:
movq -8(%rbp), %r11
push %r11
movq -8(%rbp), %r11
push %r11
pop %r10
pop %r11
cmp %r11, %r10
movq $0, %r11
movq $1, %r10
cmove %r10, %r11
movq %r11, %r11
push %r11
movq -16(%rbp), %r11
push %r11
movq -24(%rbp), %r11
push %r11
pop %r10
pop %r11
cmp %r11, %r10
movq $0, %r11
movq $1, %r10
cmove %r10, %r11
movq %r11, %r11
push %r11
pop %r10
pop %r11
and %r10, %r11
push %r11
pop %r10
movq $1, %r11
cmp %r10, %r11
jne .cf_elseif12
push .s_15
pop %rdi
movq $0, %rax
call printf
push %rax
addq $8, %rsp
.cf_endif12:
movq -8(%rbp), %r11
push %r11
movq -8(%rbp), %r11
push %r11
pop %r10
pop %r11
cmp %r11, %r10
movq $0, %r11
movq $1, %r10
cmovne %r10, %r11
movq %r11, %r11
push %r11
movq -16(%rbp), %r11
push %r11
movq -24(%rbp), %r11
push %r11
pop %r10
pop %r11
cmp %r11, %r10
movq $0, %r11
movq $1, %r10
cmove %r10, %r11
movq %r11, %r11
push %r11
pop %r10
pop %r11
and %r10, %r11
push %r11
pop %r10
movq $1, %r11
cmp %r10, %r11
jne .cf_elseif13
push .s_16
pop %rdi
movq $0, %rax
call printf
push %rax
addq $8, %rsp
.cf_endif13:
movq -8(%rbp), %r11
push %r11
movq -8(%rbp), %r11
push %r11
pop %r10
pop %r11
cmp %r11, %r10
movq $0, %r11
movq $1, %r10
cmove %r10, %r11
movq %r11, %r11
push %r11
movq -16(%rbp), %r11
push %r11
movq -24(%rbp), %r11
push %r11
pop %r10
pop %r11
cmp %r11, %r10
movq $0, %r11
movq $1, %r10
cmovne %r10, %r11
movq %r11, %r11
push %r11
pop %r10
pop %r11
and %r10, %r11
push %r11
pop %r10
movq $1, %r11
cmp %r10, %r11
jne .cf_elseif14
push .s_17
pop %rdi
movq $0, %rax
call printf
push %rax
addq $8, %rsp
.cf_endif14:
movq -8(%rbp), %r11
push %r11
movq -8(%rbp), %r11
push %r11
pop %r10
pop %r11
cmp %r11, %r10
movq $0, %r11
movq $1, %r10
cmovne %r10, %r11
movq %r11, %r11
push %r11
movq -16(%rbp), %r11
push %r11
movq -24(%rbp), %r11
push %r11
pop %r10
pop %r11
cmp %r11, %r10
movq $0, %r11
movq $1, %r10
cmovne %r10, %r11
movq %r11, %r11
push %r11
pop %r10
pop %r11
and %r10, %r11
push %r11
pop %r10
movq $1, %r11
cmp %r10, %r11
jne .cf_elseif15
push .s_18
pop %rdi
movq $0, %rax
call printf
push %rax
addq $8, %rsp
.cf_endif15:
movq -8(%rbp), %r11
push %r11
movq -8(%rbp), %r11
push %r11
pop %r10
pop %r11
cmp %r11, %r10
movq $0, %r11
movq $1, %r10
cmove %r10, %r11
movq %r11, %r11
push %r11
movq -16(%rbp), %r11
push %r11
movq -24(%rbp), %r11
push %r11
pop %r10
pop %r11
cmp %r11, %r10
movq $0, %r11
movq $1, %r10
cmove %r10, %r11
movq %r11, %r11
push %r11
pop %r10
pop %r11
or %r10, %r11
push %r11
pop %r10
movq $1, %r11
cmp %r10, %r11
jne .cf_elseif16
push .s_19
pop %rdi
movq $0, %rax
call printf
push %rax
addq $8, %rsp
.cf_endif16:
movq -8(%rbp), %r11
push %r11
movq -8(%rbp), %r11
push %r11
pop %r10
pop %r11
cmp %r11, %r10
movq $0, %r11
movq $1, %r10
cmovne %r10, %r11
movq %r11, %r11
push %r11
movq -16(%rbp), %r11
push %r11
movq -24(%rbp), %r11
push %r11
pop %r10
pop %r11
cmp %r11, %r10
movq $0, %r11
movq $1, %r10
cmove %r10, %r11
movq %r11, %r11
push %r11
pop %r10
pop %r11
or %r10, %r11
push %r11
pop %r10
movq $1, %r11
cmp %r10, %r11
jne .cf_elseif17
push .s_20
pop %rdi
movq $0, %rax
call printf
push %rax
addq $8, %rsp
.cf_endif17:
movq -8(%rbp), %r11
push %r11
movq -8(%rbp), %r11
push %r11
pop %r10
pop %r11
cmp %r11, %r10
movq $0, %r11
movq $1, %r10
cmove %r10, %r11
movq %r11, %r11
push %r11
movq -16(%rbp), %r11
push %r11
movq -24(%rbp), %r11
push %r11
pop %r10
pop %r11
cmp %r11, %r10
movq $0, %r11
movq $1, %r10
cmovne %r10, %r11
movq %r11, %r11
push %r11
pop %r10
pop %r11
or %r10, %r11
push %r11
pop %r10
movq $1, %r11
cmp %r10, %r11
jne .cf_elseif18
push .s_21
pop %rdi
movq $0, %rax
call printf
push %rax
addq $8, %rsp
.cf_endif18:
movq -8(%rbp), %r11
push %r11
movq -8(%rbp), %r11
push %r11
pop %r10
pop %r11
cmp %r11, %r10
movq $0, %r11
movq $1, %r10
cmovne %r10, %r11
movq %r11, %r11
push %r11
movq -16(%rbp), %r11
push %r11
movq -24(%rbp), %r11
push %r11
pop %r10
pop %r11
cmp %r11, %r10
movq $0, %r11
movq $1, %r10
cmovne %r10, %r11
movq %r11, %r11
push %r11
pop %r10
pop %r11
or %r10, %r11
push %r11
pop %r10
movq $1, %r11
cmp %r10, %r11
jne .cf_elseif19
push .s_22
pop %rdi
movq $0, %rax
call printf
push %rax
addq $8, %rsp
.cf_endif19:

leave
ret 
.cf_elseif19:
jmp .cf_endif19
.cf_elseif18:
jmp .cf_endif18
.cf_elseif17:
jmp .cf_endif17
.cf_elseif16:
jmp .cf_endif16
.cf_elseif15:
jmp .cf_endif15
.cf_elseif14:
jmp .cf_endif14
.cf_elseif13:
jmp .cf_endif13
.cf_elseif12:
jmp .cf_endif12
.cf_elseif11:
jmp .cf_endif11
.cf_elseif10:
jmp .cf_endif10
.cf_elseif9:
jmp .cf_endif9
.cf_elseif8:
jmp .cf_endif8
.cf_elseif7:
jmp .cf_endif7
.cf_elseif6:
jmp .cf_endif6
.cf_elseif5:
jmp .cf_endif5
.cf_elseif4:
jmp .cf_endif4
.cf_elseif3:
jmp .cf_endif3
.cf_elseif2:
jmp .cf_endif2
.cf_elseif1:
jmp .cf_endif1
.cf_elseif0:
jmp .cf_endif0


// All strings.

.data
	.s_0: .asciz "10 + 20 is %d (30)\n"
	.s_1: .asciz "10 - 20 is %d (-10)\n"
	.s_2: .asciz "10 * 20 is %d (200)\n"
	.s_3: .asciz "a < b is correct\n"
	.s_4: .asciz "a <= b is correct\n"
	.s_5: .asciz "a > b is incorrect\n"
	.s_6: .asciz "a >= b is incorrect\n"
	.s_7: .asciz "c < b is incorrect\n"
	.s_8: .asciz "c <= b is correct\n"
	.s_9: .asciz "c > b is incorrect\n"
	.s_10: .asciz "c >= b is correct\n"
	.s_11: .asciz "a == a is correct\n"
	.s_12: .asciz "a != a is incorrect\n"
	.s_13: .asciz "a == b is incorrect\n"
	.s_14: .asciz "a != b is correct\n"
	.s_15: .asciz "true and true is correct\n"
	.s_16: .asciz "false and true is incorrect\n"
	.s_17: .asciz "true and false is incorrect\n"
	.s_18: .asciz "false and false is incorrect\n"
	.s_19: .asciz "true or true is correct\n"
	.s_20: .asciz "false or true is correct\n"
	.s_21: .asciz "true or false is correct\n"
	.s_22: .asciz "false or false is incorrect\n"

// All globals.

.bss
