package com.example.calculator;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Stack;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    class ExceptionZero extends Exception {
        public void printError() {
            Toast.makeText(MainActivity.this, "Деление на 0!", Toast.LENGTH_SHORT).show();
        }
    }

    class ExceptionLessNumbers extends Exception {
        public void printError(){
            Toast.makeText(MainActivity.this, "Знак вместо символа!", Toast.LENGTH_SHORT).show();
        }
    }

    private class Operation {
        char name;
        int priority;
        public Operation(char name, int priority) {
            this.name = name;
            this.priority = priority;
        }
    }

    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.textView);
    }

    @Override
    public void onClick(View view) {
        textView.setHint("Введите выражение");
        switch (view.getId()) {
            case R.id.btn0:
                textView.setText(textView.getText() + "0");
                break;
            case R.id.btn1:
                textView.setText(textView.getText() + "1");
                break;
            case R.id.btn2:
                textView.setText(textView.getText() + "2");
                break;
            case R.id.btn3:
                textView.setText(textView.getText() + "3");
                break;
            case R.id.btn4:
                textView.setText(textView.getText() + "4");
                break;
            case R.id.btn5:
                textView.setText(textView.getText() + "5");
                break;
            case R.id.btn6:
                textView.setText(textView.getText() + "6");
                break;
            case R.id.btn7:
                textView.setText(textView.getText() + "7");
                break;
            case R.id.btn8:
                textView.setText(textView.getText() + "8");
                break;
            case R.id.btn9:
                textView.setText(textView.getText() + "9");
                break;
            case R.id.btnPlus:
                textView.setText(textView.getText() + "+");
                break;
            case R.id.btnMinus:
                textView.setText(textView.getText() + "-");
                break;
            case R.id.btnDivide:
                textView.setText(textView.getText() + "/");
                break;
            case R.id.btnMultiply:
                textView.setText(textView.getText() + "*");
                break;
            case R.id.btnPow:
                textView.setText(textView.getText() + "^");
                break;
            case R.id.btnDelete:
                StringBuilder temp = new StringBuilder(textView.getText());
                if (temp.length() > 0)
                    temp.deleteCharAt(temp.length()-1);
                textView.setText(temp);
                break;
            case R.id.btnDeleteAll:
                textView.setText("");
                break;
            case R.id.btnBracket:
                textView.setText(textView.getText() + "(");
                break;
            case R.id.btnReverseBracket:
                textView.setText(textView.getText() + ")");
                break;
            case R.id.btnResult:
                textView.setHint(String.valueOf(result(textView.getText().toString())));
                textView.setText("");
                break;
        }
    }

    public double result(String input) {
        //приоритеты: 10) (, )
        //            1) ^
        //            2) *, /
        //            3) +, -
        Stack<Double> numbers = new Stack<>();
        Stack<Operation> symbols = new Stack<>();
        String temp = "";
        double tempNumber = 0;
        for (int i = 0; i < input.length(); ++i) {
            char symbInStr = input.charAt(i);
            if (Character.isDigit(symbInStr) || temp == "" && symbInStr == '-') {
                temp += symbInStr;
            }
            if ((!Character.isDigit(symbInStr) || i == input.length()-1) && !(temp == "" && symbInStr == '-'))  {
                if (temp != "")
                    numbers.push(Double.valueOf(temp));
                temp = "";
                int priority = getPriority(symbInStr);
                if (symbInStr == '(') {
                    symbols.push(new Operation(symbInStr, priority));
                }
                else if (symbInStr == ')') {
                    //вычисление операций в скобках
                    while (symbols.peek().name != '(') {
                        try {
                            if (!doOperation(symbols, numbers, tempNumber)) return 0; //выполнение единичной операции (в случае неудачи - выход)
                        } catch (ExceptionLessNumbers exceptionLessNumbers) {
                            exceptionLessNumbers.printStackTrace();
                        }
                    }

                    symbols.pop(); //удаление '('
                }
                //если символов в стэке нет или приоритет прошлого символа выше этого и это не последний эелемент, вставляем его
                else if ((symbols.empty() || symbols.peek().priority > priority) && i != input.length()-1) {
                    symbols.push(new Operation(symbInStr, priority));
                }
                //иначе считаем все предыдущие действие, приоритет которых ниже (в реальности выше)
                else if (i != input.length()-1) {
                    while(!symbols.empty() && symbols.peek().priority <= priority) {
                        try {
                            if(!doOperation(symbols, numbers, tempNumber)) return 0; //выполнение единичной операции (в случае неудачи - выход)
                        } catch (ExceptionLessNumbers exceptionLessNumbers) {
                            exceptionLessNumbers.printError();
                            return 0;
                        }
                    }

                    symbols.push(new Operation(symbInStr, priority)); //вставка нового символа
                }
            }
        }

        tempNumber = 0;
        while (!symbols.empty()) {//если ещё остались действия
            try {
                if (!doOperation(symbols, numbers, tempNumber)) return 0; //выполнение единичной операции (в случае неудачи - выход)
            } catch (ExceptionLessNumbers exceptionLessNumbers) {
                exceptionLessNumbers.printError();
                return 0;
            }
        }

        return numbers.pop();
    }

    public double calc(double numb1, char symb, double numb2) throws ExceptionZero {
        ExceptionZero exceptionZero = new ExceptionZero();
        switch (symb) {
            case '+':
                return  numb1 + numb2;
            case '-':
                return numb1 - numb2;
            case '*':
                return numb1 * numb2;
            case '/':
                if (numb2 == 0) throw exceptionZero;
                return numb1 / numb2;
            case '^':
                return Math.pow(numb1, numb2);
        }
        return -1;
    }

    public int getPriority(char symb) {
        switch (symb) {
            case '+':
            case '-':
                return 3;
            case '*':
            case '/':
                return 2;
            case '^':
                return 1;
            case '(':
            case ')':
                return 10;
        }
        return -1;
    }

    public boolean doOperation(Stack<Operation> symbols, Stack<Double> numbers, double tempNumber) throws ExceptionLessNumbers {
        ExceptionLessNumbers exceptionLessNumbers = new ExceptionLessNumbers();
        if (numbers.size() < 2) throw exceptionLessNumbers;
        double numb2 = numbers.pop();
        double numb1 = numbers.pop();
        try {
            tempNumber = calc(numb1, symbols.pop().name, numb2);
        } catch (ExceptionZero e) {
            e.printError();
            return false;
        }
        numbers.push(tempNumber);
        return true;
    }
}

