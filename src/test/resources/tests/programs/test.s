addi x1, zero, 5
addi x2, zero, 6

nop
nop
nop

beq x1, x2, target

nop
nop

addi x3, zero, 10

target:
addi x4, zero, 20

done