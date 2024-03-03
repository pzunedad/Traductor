package Practica_5;

import java.io.File;
import java.nio.charset.Charset;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;


public class Lexico {
    // palabrasReservadas: tabla Hash de palabras reservadas
    // posicion: posición del carácter actual
    // lineas: número de líneas del programa
    // caracter: carácter actual devuelto por extraeCaracter()
    // programa: código fuente del programa
    private PalabrasReservadas palabrasReservadas;
    private int posicion;
    private int lineas;
    private char caracter;
    private String programa;


    public Lexico(String nombreArchivo, Charset charset) {
        File ficheroEntrada = new File(nombreArchivo);

        try {
            BufferedReader br = Files.newBufferedReader(ficheroEntrada.toPath(), charset);
            StringBuilder contenido = new StringBuilder();
            String linea;
            while ((linea = br.readLine()) != null) {
                contenido.append(linea).append("\n");
            }
            br.close();
            programa = contenido.toString();
        } catch (IOException e) {
            System.err.println("Error al leer el archivo: " + e.getMessage());
        }
    }

    public boolean esVector(boolean ant){
        if(this.caracter==']'){
            return false;
        }
        this.caracter=extraeCaracter();
        if(this.caracter=='['){
            devuelveCaracter();
            return true;
        }
        else{
            devuelveCaracter();
            return ant;
        }
    }

    public String getContenidoDelArchivo() {
        return programa;
    }


    public Lexico(String programa) {
        this.posicion = 0;
        this.lineas = 1;
        // la tabla Hash de palabras reservadas almacena el lexema
        // (clave) y el token (valor), la etiqueta del token coincide
        // con el lexema de la palabra reservada
        this.palabrasReservadas = new PalabrasReservadas("lexico.txt");
        // al final del programa se añade el carácter 0 para indicar
        // el final, cuando el analizador léxico encuentra este carácter
        // devuelve el token “end_program”
        this.programa = programa + (char) (0);
    }

    public void comentariosCortos(){
        //elimina los comentarios de 1 sola linea, los de //
        String linea1;
        int tamano2;
        String[] lineasP = programa.split("\n");
        linea1 = lineasP[this.lineas-1];
        String[] palabras = linea1.split("\\s+");
        tamano2 = palabras.length;
        if(tamano2<4){
            this.lineas++;
        }
        else {
            while (true) {
                if ((int) this.caracter == 13) {
                    devuelveCaracter();
                    return;
                } else if ((int) this.caracter == 10) {
                    this.lineas++;
                    devuelveCaracter();
                    return;
                }
                this.caracter = extraeCaracter();
            }
        }
    }

    public boolean inicioComL(){
            if(this.caracter == '*'){
                return true;
            }
            else {
                return false;
            }
    }
    public void comentariosLargos(){
        //cuando se detecta el /*, entra en esta funcion para pasar de caracter en caracter, hasta que encuentre el */
        while (true) {//pasar de linea
            if ((int) this.caracter == 10) {
                this.lineas++;
            }
            if(this.caracter=='*') {//puede haber un * y luego un espacio o algo
                    this.caracter = extraeCaracter();
                    if(this.caracter=='/'){
                        return;
                    }
            }
            this.caracter = extraeCaracter();
        }
    }
    public void removeSpace(){
        while (true) {
            this.caracter = extraeCaracter();
            if (this.caracter == ' ' || (int) this.caracter == 9 || (int) this.caracter == 13)
                continue;
            else if ((int) this.caracter == 10)
                this.lineas++;
            else
                break;
        }
    }


    private char extraeCaracter() {
        return this.programa.charAt(this.posicion++);
    }

    private void devuelveCaracter() {
        this.posicion--;

    }

    // extraeCaracter(char c) se usa para reconocer operadores con
    // lexemas de dos caracteres: &&, ||, <=, >=, ==, !=
    private boolean extraeCaracterD(char c) {
        if (this.posicion < this.programa.length() - 1) {
            this.caracter = extraeCaracter();
            if (c == this.caracter)

                return true;

            else {
                devuelveCaracter();
                devuelveCaracter();
                return false;
            }

        } else

            return false;
    }

    public int getLineas() {
        return this.lineas;
    }
    // la clase Character de Java ofrece los métodos:
    // - Character.isDigit(char c): devuelve true si c es un dígito
    // - Character.isLetter(char c): devuelve true si c es una letra
    // - Character.isLetterOrDigir(char c): devuelve true si c es una
    // letra o un dígito
    // estos métodos se usan para reconocer identificadores y números.
    // aplicando las expresiones regulares:
    // - id = letra (letra | digito )*
    // - numero = digito+ ( . digito+ )?

    public ComponenteLexico getComponenteLexico() {
        String etiquetaLexica;
        // el analizador léxico descarta los espacios (código 32),
        // tabuladores (código 9) y saltos de línea (códigos 10 y 13)
        while (true) {
            this.caracter = extraeCaracter();
            if (this.caracter == 0)
                return new ComponenteLexico("end_program");
            else if (this.caracter == ' ' || (int) this.caracter == 9 || (int) this.caracter == 13)
                continue;
            else if ((int) this.caracter == 10)
                this.lineas++;
            else
                break;
        }
        // secuencias de dígitos de números enteros o reales
        if (Character.isDigit(this.caracter)) {
            String numero = "";
            do {
                numero = numero + this.caracter;
                this.caracter = extraeCaracter();
            } while (Character.isDigit(this.caracter));
            if (this.caracter != '.') {
                devuelveCaracter();
                return new ComponenteLexico("int", numero);
            }

            do {
                numero = numero + this.caracter;
                this.caracter = extraeCaracter();
            } while (Character.isDigit(this.caracter));
            devuelveCaracter();
            return new ComponenteLexico("float", numero);
        }

        // identificadores y palabras reservadas
        if (Character.isLetter(this.caracter)) {
            String lexema = "";
            do {
                lexema = lexema + this.caracter;
                this.caracter = extraeCaracter();
            } while (Character.isLetterOrDigit(this.caracter));

            devuelveCaracter();
            if (this.palabrasReservadas.getEtiqueta(lexema)!=null)
                return new ComponenteLexico((String)this.palabrasReservadas.getEtiqueta(lexema));
            else
                return new ComponenteLexico("id", lexema);
        }
        // operadores aritméticos, relacionales, lógicos y
        // caracteres delimitadores
        // operadores aritméticos, relacionales, lógicos
        // y caracteres delimitadores
        String lexema = "", lexemaAlternativo, etiquetaAlternativa;
        do {
            lexema = lexema + this.caracter;
            if(this.caracter == '&'){//detectar and
                this.caracter = extraeCaracter();
                if(this.caracter == '&'){
                    lexema = lexema + this.caracter;
                }
            }
            if(this.caracter == '|'){//detectar or
                this.caracter = extraeCaracter();
                if(this.caracter == '|'){
                    lexema = lexema + this.caracter;
                }
            }
            etiquetaLexica = palabrasReservadas.getEtiqueta(lexema);
            if (etiquetaLexica == null)
                return new ComponenteLexico("invalid_char");
            else {
                lexemaAlternativo = lexema;
                this.caracter = extraeCaracter();
                lexemaAlternativo = lexemaAlternativo + this.caracter;
                etiquetaAlternativa = palabrasReservadas.getEtiqueta(lexemaAlternativo);
                if (etiquetaAlternativa != null)
                    etiquetaLexica = etiquetaAlternativa;
            }
        } while (etiquetaAlternativa != null);
        devuelveCaracter();
        return new ComponenteLexico(etiquetaLexica);
    }
}