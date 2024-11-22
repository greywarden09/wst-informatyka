#pragma once
#include <stdexcept>

#include "../common/list.hpp"

template<typename T>
class LinkedList final : public List<T> {
    struct Node {
        T data;
        Node *next;
        Node *prev;

        Node(Node *prev, T data, Node *next) : data(data), next(next), prev(prev) {
        }
    };

    Node *first;
    Node *last;
    uint32_t _size;

    void checkElementIndex(const uint32_t index) const {
        if (index >= _size) {
            throw std::out_of_range("index out of range");
        }
    }

    Node *node(const uint32_t index) {
        checkElementIndex(index);
        if (index < (_size >> 1)) {
            auto x = first;
            for (auto i = 0; i < index; i++) {
                x = x->next;
            }
            return x;
        }
        auto x = last;
        for (auto i = _size - 1; i > index; i--) {
            x = x->prev;
        }
        return x;
    }

    void linkLast(T value) {
        auto l = last;
        auto newNode = new Node(l, value, nullptr);
        last = newNode;
        if (l == nullptr) {
            first = newNode;
        } else {
            l->next = newNode;
        }
        _size++;
    }

    void linkBefore(T value, Node *node) {
        auto pred = node->prev;
        auto newNode = new Node(pred, value, node);
        node->prev = newNode;
        if (pred == nullptr) {
            first = newNode;
        } else {
            pred->next = newNode;
        }
        _size++;
    }

    void unlink(Node *node) {
        auto next = node->next;
        auto prev = node->prev;

        if (prev == nullptr) {
            first = next;
        } else {
            prev->next = next;
        }

        if (next == nullptr) {
            last = prev;
        } else {
            next->prev = prev;
        }

        delete node;
        _size--;
    }

public:
    LinkedList(): first(nullptr), last(nullptr), _size(0) {
    }

    LinkedList(std::initializer_list<T> initList): LinkedList() {
        for (const auto &item : initList) {
            add(item);
        }
    }

    ~LinkedList() override {
        clear();
    }

    void add(T value) override {
        linkLast(value);
    }

    void checkPositionIndex(const uint32_t index) const {
        if (index > _size) {
            throw std::out_of_range("index out of range");
        }
    }

    void add(const uint32_t index, T value) override {
        checkPositionIndex(index);
        if (index == _size) {
            linkLast(value);
        } else {
            linkBefore(value, node(index));
        }
    }

    void set(const uint32_t index, T value) override {
        checkElementIndex(index);
        auto x = node(index);
        x->data = value;
    }

    T get(const uint32_t index) override {
        checkElementIndex(index);
        return node(index)->data;
    }

    void remove(T value) override {
        auto x = first;
        while (x != nullptr) {
            auto next = x->next;
            if (value == x->data) {
                unlink(x);
            }
            x = next;
        }
    }

    void clear() override {
        auto current = first;
        while (current) {
            auto next = current->next;
            delete current;
            current = next;
            _size--;
        }
        first = last = nullptr;
    }

    uint32_t size() const override {
        return _size;
    }

    int32_t indexOf(T value) const override {
        auto i = 0;
        for (auto x = first; x != nullptr; x = x->next, i++) {
            if (value == x->data) {
                return i;
            }
        }
        return -1;
    }

    T *toArray() override {
        auto array = new T[_size];
        uint32_t index = 0;
        for (auto x = first; x != nullptr; x = x->next, index++) {
            array[index] = x->data;
        }
        return array;
    }

    void removeAt(const uint32_t index) override {
        checkElementIndex(index);
        auto x = first;
        for (int i = 0; i < index; i++) {
            x = x->next;
        }
        unlink(x);
    }
};
