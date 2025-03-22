#!/usr/bin/env python3

from sympy import symbols, SOPform, POSform, simplify_logic


def main():
    n = int(input("Variables count: "))
    var_names = input("Variable names separated by space: ").split()
    vars = symbols(" ".join(var_names))

    values = []
    for x in range(0, 2 ** n):
        bin_str = bin(x)[2:].zfill(n)
        values.append(int(input(format(f"{bin_str}: "))))

    minterms = [i for i, v in enumerate(values) if v == 1]
    maxterms = [i for i, v in enumerate(values) if v == 0]

    print()

    print("--- MINTERMS ---")
    print(f"m({', '.join(map(str, minterms))})")

    print("--- MAXTERMS ---")
    print(f"m({', '.join(map(str, maxterms))})")

    print()

    sop = SOPform(vars, minterms)
    print(f"SOP form: {sop}")

    pos = POSform(vars, minterms)
    print(f"POS form: {pos}")

    simplified_sop = simplify_logic(sop, form='dnf')
    print(f"Simplified SOP: {simplified_sop}")

    simplified_pos = simplify_logic(pos, form='dnf')
    print(f"Simplified POS: {simplified_pos}")

if __name__ == "__main__":
    main()
