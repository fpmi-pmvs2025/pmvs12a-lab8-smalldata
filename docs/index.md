
# Персональный менеджер учёта расходов

---

## 🏠 Главная страница

Мобильное приложение для учета личных финансов с функциями:
- Внесение доходов/расходов с категоризацией
- Автоматическая конвертация валют в USD
- Визуальная аналитика (графики и диаграммы)

**Быстрые ссылки**:
- [Функциональные требования](#🔧-функциональные-требования)
- [Диаграмма файлов](#📁-диаграмма-файлов-приложения)
- [Спецификация](#⚙️-дополнительная-спецификация)

---

## 🔧 Функциональные требования

### Основные функции
1. **Добавление транзакций**:
   - Выбор типа (доход/расход)
   - Ввод суммы с выбором валюты (USD, BYN, USDT)
   - Указание категории и описания

2. **Конвертация валют**:
   ```mermaid
   flowchart LR
     A[BYN] -->|Курс ЦБ| B(USD)
     C[USDT] -->|Курс биржи| B
   ```

3. **Статистика**:
   - Круговые диаграммы по категориям
     ![image](https://github.com/user-attachments/assets/ee5589fc-b315-4a12-a291-b9866c898e54)

   - Линейные графики за выбранный период

### Use Case диаграмма
```mermaid
graph TD
  User[Пользователь] --> Add(Добавить транзакцию)
  User --> Stats(Просмотреть статистику)
  User --> Settings(Настроить валюту)
  Add --> Validate{Валидация}
  Validate -->|Success| Save[Сохранение]
```

### Сценарий использования
```gherkin
Сценарий: Добавление расхода
  Дано Я на главном экране приложения
  Когда Я нажимаю "+"
  И Выбираю "Расход"
  И Ввожу сумму 3500 BYN
  И Указываю категорию "Техника"
  Тогда Транзакция сохраняется
  И Баланс уменьшается на $1000 (по курсу)
```

---

## 📁 Диаграмма файлов приложения

```plaintext
pmvs12a-lab8-smalldata/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/expensemanager/
│   │   │   │   ├── di/                # Dependency Injection (Koin/Dagger)
│   │   │   │   ├── model/
│   │   │   │   │   ├── Transaction.kt # Data class
│   │   │   │   │   └── Currency.kt    
│   │   │   │   ├── repository/
│   │   │   │   │   ├── CurrencyRepository.kt # API для курсов валют
│   │   │   │   │   └── TransactionRepository.kt 
│   │   │   │   ├── ui/
│   │   │   │   │   ├── MainActivity.kt
│   │   │   │   │   ├── adapters/
│   │   │   │   │   │   └── TransactionAdapter.kt 
│   │   │   │   │   ├── viewmodels/
│   │   │   │   │   │   └── MainViewModel.kt 
│   │   │   │   │   └── fragments/
│   │   │   │   │       ├── AddTransactionFragment.kt
│   │   │   │   │       └── StatsFragment.kt
│   │   │   │   ├── utils/
│   │   │   │   │   └── CurrencyConverter.kt # Логика конвертации
│   │   │   ├── res/
│   │   │   │   ├── layout/
│   │   │   │   │   ├── activity_main.xml
│   │   │   │   │   └── fragment_add_transaction.xml
│   │   │   │   ├── values/
│   │   │   │   │   ├── strings.xml
│   │   │   │   │   └── colors.xml
│   │   │   │   └── drawable/
│   │   │   └── AndroidManifest.xml
│   │   ├── test/      # Unit тесты
│   │   └── androidTest/ # Интеграционные тесты
│   └── build.gradle   # Конфигурация модуля
├── build.gradle       # Корневой конфигурационный файл
└── settings.gradle    # Настройки проекта
```

---

## Диаграммы
### Диаграмма классов
![image](https://github.com/user-attachments/assets/c0f98f04-fa2d-405a-8c5e-93d4c731178e)
### Диаграмма последвательности
![image](https://github.com/user-attachments/assets/4681921e-5caf-47a7-ba89-c6d659315826)


## ⚙️ Дополнительная спецификация

### Ограничения
- Поддерживаемые валюты: USD, BYN, USDT
- Макс. сумма: $1,000,000 за операцию

### Требования безопасности
```mermaid
graph LR
  A[Данные] --> B[Шифрование AES-256]
  B --> C[Сохранение в PostrgeSql]
```

### Надежность
- Автосохранение каждые 60 секунд
- Резервное копирование в облако

### Диаграмма БД
![image](https://github.com/user-attachments/assets/d4b90130-c2cd-4ac3-9cce-1aca4827f5f5)


---

[⬆️ К началу](#🏠-главная-страница)
```


