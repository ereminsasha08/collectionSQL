Вступительное задание

Реализация некоторого подобия языка управления данными в коллекции. Основные команды, которые должны поддерживаться это вставка элементов в коллекцию, удаление элемента из коллекции, поиск элементов в коллекции, изменение элементов в коллекции.
Структура коллекции заранее определена.

Описание задачи:
Коллекция данных в данной задаче это структура представляющая собой некоторую таблицу данных, в которой есть наименования колонок и каждая строчка в таблице это элемент коллекции.
Необходимо реализовать метод, который на вход получает команду в виде строки (требования к формату будет ниже). Команда должна выполнять четыре основные операции  вставка, изменение, поиск и удаление элементов из коллекции данных.
Также при изменении, удалении и поиске должны поддерживаться условия выборки из коллекции (ниже они будут представлены).

На выход список элементов в коллекции, которые были найдены, либо которые были изменены, либо которые были добавлены, либо которые были удалены.


Требования к задаче:

Требования к коллекции:

Коллекция представляет из себя таблицу в виде List<Map<String,Object>> . Где List это список строк в таблице. Map это значения в колонках, где ключом Map является наименование колонки в виде строки, а значением Map является значение в ячейке таблицы (допустимые типы значений в ячейках: Long, Double, Boolean, String).

Уникальность значений в ячейках не проверяется, т.е. может быть две записи с id=1 или две записи с lastName=”Иванов”. Также некоторые значения могут быть пустыми, но все значения в ячейках в одной строчке пустыми быть не могут.




Требования к формату запроса с командой, поступающей на вход:
Возможные команды:
INSERT - вставка элемента в коллекцию,
UPDATE  изменение элемента в коллекции,
DELETE - удаление элемента из коллекции,
SELECT - поиск элементов в коллекции.

Возможные операторы сравнения:
Перед началом оператора сравнения должен стоять оператор WHERE - оператор, который говорит, что команда должна выполняться с условием выборки.


