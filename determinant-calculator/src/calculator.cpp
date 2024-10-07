#include <vector>
#include <cassert>

using Matrix = std::vector<std::vector<double> >;

namespace Laplace {
    Matrix getMinor(const Matrix &matrix, const uint32_t &row, const uint32_t &col) {
        const auto n = matrix.size();
        Matrix minor(n - 1, std::vector<double>(n - 1));
        for (auto i = 0, mi = 0; i < n; ++i) {
            if (i == row) {
                continue;
            }

            for (auto j = 0, mj = 0; j < n; ++j) {
                if (j == col) {
                    continue;
                }
                minor[mi][mj] = matrix[i][j];
                mj++;
            }
            mi++;
        }
        return minor;
    }

    double determinant(const Matrix &matrix) {
        const auto n = matrix.size();
        assert(n > 1);
        if (n == 2) {
            return matrix[0][0] * matrix[1][1] - matrix[0][1] * matrix[1][0];
        }

        auto det = 0.0;
        for (auto i = 0; i < n; i++) {
            auto minor = getMinor(matrix, 0, i);
            det += (i % 2 == 0 ? 1 : -1) * matrix[0][i] * determinant(minor);
        }
        return det;
    }
}


