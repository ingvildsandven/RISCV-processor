main:
    call function
    addi x6, zero, 20
    j done

function:
    addi x5, zero, 10
    jr ra

    done