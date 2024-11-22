#include <gtest/gtest.h>

#include "../../main/linked_list/linked_list.hpp"

TEST(LINKED_LIST_TEST, testEmptyLinkedList) {
    const auto list = LinkedList<int>();

    ASSERT_EQ(list.size(), 0);
}

TEST(LINKED_LIST_TEST, testInitializerListConstructor) {
    auto list = LinkedList{1, 2, 3, 4, 5};

    ASSERT_EQ(list.size(), 5);

    for (auto i = 0; i < list.size(); i++) {
        ASSERT_EQ(list[i], i + 1);
    }
}

TEST(LINKED_LIST_TEST, testAddingElement) {
    auto list = LinkedList<int>();

    list += 5;
    list += 10;

    ASSERT_EQ(list.size(), 2);
    ASSERT_EQ(list[0], 5);
    ASSERT_EQ(list[1], 10);
}

TEST(LINKED_LIST_TEST, testAddingElementAtIndex) {
    auto list = LinkedList{1, 2, 3};

    list.add(0, 5);

    ASSERT_EQ(list.size(), 4);
    ASSERT_EQ(list[0], 5);
    ASSERT_EQ(list[1], 1);
    ASSERT_EQ(list[2], 2);
    ASSERT_EQ(list[3], 3);

    list.add(2, 10);

    ASSERT_EQ(list.size(), 5);
    ASSERT_EQ(list[0], 5);
    ASSERT_EQ(list[1], 1);
    ASSERT_EQ(list[2], 10);
    ASSERT_EQ(list[3], 2);
    ASSERT_EQ(list[4], 3);

    EXPECT_THROW({list.add(6, 10);}, std::out_of_range);
}

TEST(LINKED_LIST_TEST, testSetElementAtIndex) {
    auto list = LinkedList{1, 2, 3};
    list.set(0, 5);

    ASSERT_EQ(list[0], 5);

    EXPECT_THROW({list.set(5, 10);}, std::out_of_range);
}

TEST(LINKED_LIST_TEST, testGetElement) {
    auto list = LinkedList{1, 2, 3};

    ASSERT_EQ(list.get(0), 1);
    ASSERT_EQ(list.get(1), 2);
    ASSERT_EQ(list.get(2), 3);

    ASSERT_EQ(list[0], 1);
    ASSERT_EQ(list[1], 2);
    ASSERT_EQ(list[2], 3);

    EXPECT_THROW({list.get(5);}, std::out_of_range);
}

TEST(LINKED_LIST_TEST, testRemoveElement) {
    auto list = LinkedList{1, 2, 3};

    list.remove(0);
    ASSERT_EQ(list.size(), 3);

    list.remove(2);
    ASSERT_EQ(list.size(), 2);

    ASSERT_EQ(list[0], 1);
    ASSERT_EQ(list[1], 3);
}

TEST(LINKED_LIST_TEST, testRemoveElementAtIndex) {
    auto list = LinkedList{1, 2, 3};

    list.removeAt(2);
    ASSERT_EQ(list.size(), 2);

    ASSERT_EQ(list[0], 1);
    ASSERT_EQ(list[1], 2);

    EXPECT_THROW({list.removeAt(5);}, std::out_of_range);
}

TEST(LINKED_LIST_TEST, testClearList) {
    auto list = LinkedList{1, 2, 3};
    list.clear();

    ASSERT_EQ(list.size(), 0);

    EXPECT_THROW({list.get(0);}, std::out_of_range);
    EXPECT_THROW({list.get(1);}, std::out_of_range);
    EXPECT_THROW({list.get(2);}, std::out_of_range);
}

TEST(LINKED_LIST_TEST, testIndexOf) {
    const auto list = LinkedList{1, 2, 3};

    ASSERT_EQ(list.indexOf(2), 1);
    ASSERT_EQ(list.indexOf(15), -1);
}

TEST(LINKED_LIST_TEST, testToArray) {
    auto list = LinkedList{1, 2, 3};
    const auto array = list.toArray();

    ASSERT_EQ(list[0], array[0]);
    ASSERT_EQ(list[1], array[1]);
    ASSERT_EQ(list[2], array[2]);

    for (auto i = 0; i < list.size(); i++) {
        array[i] *= 10;
    }

    ASSERT_EQ(list[0], 1);
    ASSERT_EQ(list[1], 2);
    ASSERT_EQ(list[2], 3);

    delete[] array;
}
