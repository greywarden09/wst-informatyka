#!/usr/bin/env python3

def prompt_for_input(prompt, validate, on_error='invalid arg'):
    tmp = input(prompt)
    if not validate(tmp):
        print(on_error)
        prompt_for_input(prompt, on_error, validate)
    return tmp


def is_int(arg):
    try:
        int(arg)
        return True
    except ValueError:
        return False


def is_float(arg):
    try:
        float(arg)
        return True
    except ValueError:
        return False


def get_minor(matrix, i, j):
    return [row[:j] + row[j + 1:] for row in (matrix[:i] + matrix[i + 1:])]


def determinant(matrix):
    n = matrix.__len__()
    assert n > 1
    if n == 2:
        return matrix[0][0] * matrix[1][1] - matrix[0][1] * matrix[1][0]

    det = 0.0
    for i in range(n):
        minor = get_minor(matrix, 0, i)
        det += (1 if i % 2 == 0 else -1) * matrix[0][i] * determinant(minor)
    return det


def main():
    rank = int(prompt_for_input('Matrix rank: ', lambda x: is_int(x)))
    matrix = [[0 for _ in range(rank)] for _ in range(rank)]
    for i, j in ((i, j) for i in range(rank) for j in range(rank)):
        matrix[i][j] = float(prompt_for_input(f"a[{i + 1}, {j + 1}] = ", lambda x: is_float(x)))

    print(f"det(A) = {determinant(matrix)}")


if __name__ == '__main__':
    main()
