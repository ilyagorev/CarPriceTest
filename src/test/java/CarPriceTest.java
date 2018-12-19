/**
 * Created by ilyag on 18/12/18.
 */

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.SelenideElement;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.codeborne.selenide.Condition.attribute;
import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selectors.*;
import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.sleep;

/*
 * Задача: автотест проверяет сценарий “Пользователь может отправить
 * письмо-заявку, пройдя два шага мастера заполнения форм”.
*/

public class CarPriceTest {

    @BeforeClass
    public static void setup() {
        Configuration.timeout = 6000;
        Configuration.baseUrl = "https://carprice.ru";
        Configuration.browser = "chrome";
    }

    @Test
    public void userCanSendOrderMail () {

        //Открыть браузер и загрузить главную страницу
        open("/");

        //Так как на главной странице реализовано два сценария для заполнения формы первого шага,
        //сохраню элемент формы первого раздела и дальнейшие действия буду выполнять с ее дочерними элементами.
        //Также проверю, что форма успешно загружена и присутствует в DOM.
        SelenideElement formEvaluateStep1 = $(byName("evaluateStep1")).should(exist);

        //Решил сэмулировать клик по элементу списка и поиск по тексту, а не прямое присвоение значений, считаю, что
        //такой вариант ближе к поведению реального пользователя. Это ограничение также определяет выбор методов
        //взаимодействия с элементами интерфейса в дальнейшем по ходу выполнения теста.

        //Выбираю элемент списка "Марка"
        formEvaluateStep1.find(byText("Марка")).click();
        formEvaluateStep1.find(byText("Audi")).click();
        // Альтернатива:
        // formEvaluateStep1.find(byClassName("react-select-evaluate-step1-first-item")).click();
        // formEvaluateStep1.find("#react-select-2--option-145").click();

        //Также на этом этапе можно сделать доп. тесты на проверку живого поиска (сверям введеный слог-> смотрим отфильтрованный список),
        //корректность отображение списка (появление <div class="Select-menu-outer">), сравнение списка с исходными данными,
        //но считаю, что каждый из кейсов нужно выделять в самостоятельный сценарий.

        // Так как загрузка каждого последующего списка происходят динамически и поля выбора закрыты, необходима дополнительная
        // проверка доступности, для этого можно пойти несколькими путями:
        // 1) У каждого <div "form-group"> <div class="Select .../> .../> существует класс is-disabled, который можно отслеживать,
        // но в таком случае придется работать с хрупким селектором, т.к любая обертка, напр. в целях правок дизайна приведет к поломке теста.
        // 2) Отслеживать появления дочернего элемента input
        //    $("#react-select-3--value > div.Select-input > input").should(exist);
        // В таком варианта невозможна унификация подходов для 1й и 2й страниц, т.к. на аналогичном действии на
        // второй странице отсутствует вложенный элемент <input>.
        // 3) Ожидание по фиксированному таймеру и непосредвенное обращение к элементу.
        // 4) Ожидание отображения блока div.Select-input - уникальные идентификаторы отсутвуют => аналогично п.1.
        // 5) Отслеживать атрибуты aria-disabled, но и здесь есть нюанс, логика для этого aria атрибует отличается для 1й и 2й страниц:
        //    5.1.) На 1й странице состония элемент "Не доступен" == aria-disabled:true;
        //                                          "Доступен" == атрибут отсутсвует.
        //    5.2.) На 2й странице состония элемент "Не доступен" == aria-disabled:true;
        //                                          "Доступен" == aria-disabled:false.
        // Но даже при этом расхождении логики, этот вариант выглядет наиболее оптимально.

        // Проверяю доступность списка "Год" и выбираю элемент
        formEvaluateStep1.find(byAttribute("aria-activedescendant", "react-select-3--value"))
                                            .shouldNotHave(attribute("aria-disabled", "true"));
        formEvaluateStep1.find(byText("Год")).click();
        formEvaluateStep1.find(byText("2016")).click();

        // Проверяю доступность списка "Модель" и выбираю элемент
        formEvaluateStep1.find(byAttribute("aria-activedescendant", "react-select-4--value"))
                                            .shouldNotHave(attribute("aria-disabled", "true"));
        formEvaluateStep1.find(byText("Модель")).click();
        formEvaluateStep1.find(withText("A5")).click();

        //Для заполения формы выбрал метод sendKey вместо прямого присваивания setInput, чтобы сэмулировать ввод с клавиатуры
        formEvaluateStep1.find(byName("email")).sendKeys("test@test.ru");

        //Отправляю форму кликом по кнопке [Далее]
        formEvaluateStep1.find(byName("submit")).click();

        //Определяю готовность формы на странице второго шага
        $(byName("evaluateStep2")).should(exist);

        SelenideElement formEvaluateStep2 = $(byName("evaluateStep2"));

        //Т.к. для выбора города вновь нужно использовать чтобы обойти подбор по селектору, определяю область которая
        String currentCity = $(byClassName("js-current-city")).getText();

        // Раскрываю список городов и выбираю элемент списка
        formEvaluateStep2.find(byText(currentCity)).click();
        formEvaluateStep2.find(byText("Санкт-Петербург")).click();

        // Раскрываю список городов и выбираю элемент списка
        formEvaluateStep2.find(byText("Выберите офис")).click();

        // Отлавливаю список в модальном окне
        SelenideElement modalWindowSearchList = $(byClassName("search-list__list"));
        modalWindowSearchList.should(exist);

        // Выбираю адрес офиса
        modalWindowSearchList.find(withText("Пархоменко, 7")).click();

        // Чтобы не усложенять логику определения даты (думал брать сегодняшний день, добавлять какой-то период
        // и преобразовывать в вид, аналогичный форме) решил искать вхождения текста в строке и т.к. диапазон поиска
        // ограничен формой, ложные срабатывания исключены
        $(byAttribute("aria-activedescendant", "react-select-10--value"))
                        .shouldHave(attribute("aria-disabled", "false"));
        formEvaluateStep2.find(byText("Выберите дату")).click();
        formEvaluateStep2.find(withText("Пн")).click();

        // Выбираю время
        $(byAttribute("aria-activedescendant", "react-select-11--value"))
                .shouldHave(attribute("aria-disabled", "false"));
        formEvaluateStep2.find(byText("Выберите время")).click();
        formEvaluateStep2.find(byText("12:00")).click();

        // Сценарий "Сегодня понедельник 18:00" проверил, ошибки нет - выбирается Пн на неделю позже

        // Поля "Имя" и "Телефон" пришлось искать по атрубуту placeholder, т.к. уникальные идентификатора полей отсутствуют
        formEvaluateStep2.find(byAttribute("placeholder", "Имя")).sendKeys("Тестов Тест Тестович");
        formEvaluateStep2.find(byAttribute("placeholder", "Телефон")).sendKeys("9876543210");

        formEvaluateStep2.find(byName("submit")).click();

        sleep(15000); // искуственная задержка перед открытием финальной страницы

        //Проверю, что финальная страница появилась и сообщает об успешной  записи
        $(".ticket__title").shouldHave(text("Вы успешно записались"));
        sleep(5000);
        //Здесь можно усложнить и проверить всю строку, с полным совпадением всех текстовых значений.
        //Но для полнотекстовой сверки необходимо вынести зафиксированные константами строки какую-либо структуру данных,
        //либо внешний конфиг файл, чтобы правки верстки, изменения справочников не требовали корректировок кода теста.

        //Также из мыслей об оптимизации - в текущей версии тест выполнен без использования инкапсуляции и
        //не придеживается шаблона Page Objects, для дальнейшего развития требуется рефакторинг
    }
}

/*
 Данное решение, по-моему мнению, выглядит сложно поддерживаемым. Вариантом оптимизации является
 работа с максимально независимыми CSS-селекторами \ XPath-локаторами и уникальными идентификаторами.
 Создание и поддержку подобного теста может оптимизировать разметка страниц, придерживающаяся к-либо стандарта
 по именованию и идентификации элементов, напр. методологии БЭМ, правила которой встречались на некоторых элементах.
 Также помогла бы унификация логики поведения элементов интерфейса (пример с aria-disabled="true" и тег отсутвует \ aria-disabled="false").
*/

