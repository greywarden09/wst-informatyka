#include <iostream>
#include <format>

using std::cout;
using std::endl;
using std::cin;
using std::format;

typedef std::string String;

inline String formatArgs(const double&, const double&, const double&);
inline double promptForInput(const String &);
inline double calculateDelta(const double&, const double&, const double&);

int main() {
    const auto a = promptForInput("a: ");
    const auto b = promptForInput("b: ");
    const auto c = promptForInput("c: ");

    if (const auto delta = calculateDelta(a, b, c); delta < 0) {
        cout << "Delta is negative." << endl;
    } else if (delta == 0) {
        const auto x0 = -1.0 * b / 2 * a;
        cout << format("{}\tx0 = {}", formatArgs(a, b, c), x0 == 0.0 ? abs(x0) : x0) << endl;
    } else {
        const auto deltaSqrt = sqrt(delta);
        const auto x1 = (-1.0 * b - deltaSqrt) / 2 * a;
        const auto x2 = (-1.0 * b + deltaSqrt) / 2 * a;
        cout << format("{}\tx1 = {}, x2 = {}",
            formatArgs(a, b, c),
            x1 == 0.0 ? abs(x1) : x1,
            x2 == 0.0 ? abs(x2) : x2)
        << endl;
    }

    return 0;
}

inline String formatArgs(const double& a, const double& b, const double& c) {
    return std::format("[a = {}, b = {}, c = {}]", a, b, c);
}

inline double promptForInput(const String &prompt) {
    double tmp;
    while (true) {
        cout << prompt;
        cin >> tmp;
        if (cin.fail()) {
            cout << "invalid arg" << endl;
            cin.clear();
            cin.ignore(std::numeric_limits<std::streamsize>::max(), '\n');
        } else {
            return tmp;
        }
    }
}

inline double calculateDelta(const double& a, const double& b, const double& c) {
    return b * b - 4 * a * c;
}
