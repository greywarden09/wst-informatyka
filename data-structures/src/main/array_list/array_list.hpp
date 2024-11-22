#pragma once
#include <type_traits>
#include <cstring>
#include <memory>

#include "../common/list.hpp"

#define ARRAY_LIST_INITIAL_CAPACITY 16

template<typename T>
class ArrayList final : public List<T> {
    T *data;
    uint32_t _size;
    uint32_t capacity;

    void grow() {
        const auto newCapacity = capacity < 64 ? capacity + ARRAY_LIST_INITIAL_CAPACITY : capacity + capacity / 2;
        auto newArray = new T[newCapacity];

        if constexpr (std::is_trivially_copyable_v<T>) {
            std::memcpy(newArray, data, sizeof(T) * _size);
        } else {
            for (auto i = 0; i < _size; i++) {
                newArray[i] = std::move(data[i]);
            }
        }
        delete[] data;
        data = newArray;
        capacity = newCapacity;
    }

    void checkIndex(const uint32_t index) const {
        if (index > _size) {
            throw std::out_of_range("Array index out of range");
        }
    }

public:
    ArrayList(): _size(0), capacity(ARRAY_LIST_INITIAL_CAPACITY) {
        data = new T[capacity];
    }

    ArrayList(std::initializer_list<T> initList) {
        _size = initList.size();
        capacity = _size;
        data = new T[capacity];

        if constexpr (std::is_trivially_copyable_v<T>) {
            std::memcpy(data, initList.begin(), _size * sizeof(T));
        } else {
            std::uninitialized_copy(initList.begin(), initList.end(), data);
        }
    }

    ~ArrayList() override {
        delete[] data;
    }

    void add(T value) override {
        if (_size == capacity) {
            grow();
        }
        data[_size++] = value;
    }

    void add(const uint32_t index, T value) override {
        checkIndex(index);
        if (_size == capacity) {
            grow();
        }
        for (auto i = _size; i > index; i--) {
            data[i] = std::move(data[i - 1]);
        }
        data[index] = std::move(value);
        _size++;
    }

    void set(const uint32_t index, T value) override {
        checkIndex(index);
        data[index] = std::move(value);
    }

    T get(const uint32_t index) override {
        if (index >= _size) {
            throw std::out_of_range("Array index out of range");
        }
        return data[index];
    }

    void remove(T value) override {
        for (auto i = 0; i < _size; i++) {
            if (data[i] == value) {
                for (auto j = i; j < _size; j++) {
                    data[j] = std::move(data[j + 1]);
                }
                _size--;
            }
        }
    }

    void removeAt(const uint32_t index) override {
        if (index >= _size) {
            throw std::out_of_range("Array index out of range");
        }

        for (auto i = index; i < _size - 1; i++) {
            data[i] = std::move(data[i + 1]);
        }
        _size--;
    }

    void clear() override {
        for (auto i = 0; i < _size; ++i) {
            if constexpr (std::is_trivially_destructible_v<T>) {
                data[i] = T();
            } else {
                data[i].~T();
            }
        }
        _size = 0;
    }

    uint32_t size() const override {
        return _size;
    }

    int32_t indexOf(T value) const override {
        for (auto i = 0; i < _size; i++) {
            if (data[i] == value) {
                return i;
            }
        }
        return -1;
    }

    T *toArray() override {
        auto result = new T[_size];
        memcpy(result, data, _size * sizeof(T));
        return result;
    }
};
