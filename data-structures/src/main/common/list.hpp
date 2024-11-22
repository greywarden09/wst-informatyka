#pragma once
#include <cstdint>

template<typename T>
class List {
public:
    virtual ~List() = default;

    List() = default;

    virtual void add(T value) = 0;

    virtual void add(uint32_t index, T value) = 0;

    virtual void set(uint32_t index, T value) = 0;

    virtual T get(uint32_t index) = 0;

    virtual void remove(T value) = 0;

    virtual void removeAt(uint32_t index) = 0;

    virtual void clear() = 0;

    virtual uint32_t size() const = 0;

    virtual int32_t indexOf(T value) const = 0;

    virtual T *toArray() = 0;

    virtual bool isEmpty() const {
        return size() == 0;
    }

    T operator[](const uint32_t &index) {
        return get(index);
    }

    virtual List &operator+=(T value) {
        add(value);
        return *this;
    }

    virtual List &operator-=(T value) {
        remove(value);
        return *this;
    }
};
