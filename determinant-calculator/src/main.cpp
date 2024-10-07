#include <functional>
#include <iostream>

using std::cout;
using std::cin;
using std::endl;
using std::unique_ptr;
using std::function;
using std::function;
using std::shared_ptr;

using Matrix = std::vector<std::vector<double> >;
using String = std::string;

template<typename T>
T promptForInput(const String &, function<bool(T)>);

extern double determinant(const Matrix &);

int main() {
    const auto rank = promptForInput<uint32_t>("Matrix rank: ", [](const uint32_t &r) { return r > 1; });
    auto matrix = std::vector(rank, std::vector<double>(rank));

    for (auto i = 0; i < rank; i++) {
        for (auto j = 0; j < rank; j++) {
            const auto prompt = std::format("a[{},{}] = ", i + 1, j + 1);
            matrix[i][j] = promptForInput<double>(prompt, [](auto) { return true; });
        }
    }

    cout << std::format("det(A) = {}", determinant(matrix)) << endl;

    return 0;
}

template<typename T>
T promptForInput(const String &prompt, function<bool(T)> validate) {
    T tmp;
    while (true) {
        cout << prompt;
        cin >> tmp;
        if (cin.fail() || !validate(tmp)) {
            cout << "invalid arg" << endl;
            cin.clear();
            cin.ignore(std::numeric_limits<std::streamsize>::max(), '\n');
        } else {
            return tmp;
        }
    }
}
