package Practica_5;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class TestAnalizadorSintactico {

    public static String leerTxt(String ruta){//esto lee el txt de programa
        StringBuilder contenido = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader(ruta))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                contenido.append(linea).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return contenido.toString();
    }



        public static void main (String[] args) {

        boolean mostrarComponentesLexicos = false; //poner a false y no se quieren mostrar los tokens <id, a> ...

        String expresion = leerTxt("programa.txt");

        ComponenteLexico etiquetaLexica;
        Lexico lexico = new Lexico(expresion);

        if(mostrarComponentesLexicos) {

            do {
                etiquetaLexica = lexico.getComponenteLexico();
                System.out.println("<" + etiquetaLexica.toString() + ">"); //System.out.println(etiquetaLexica.toString());

            }while(!etiquetaLexica.getEtiqueta().equals("end_program"));

            System.out.println("");
        }

        AnalizadorSintactico compilador = new AnalizadorSintactico (new Lexico(expresion));

        System.out.println("Programa introducido");
        System.out.println(expresion);
        compilador.analisisSintactico();
        if(compilador.getCorrecto()) {
            System.out.println("\nPrograma compilado correctamente\n");
        }
        else{
            System.out.println("\nLa compilacion ha fallado\n");
        }
        System.out.println("Tabla de símbolos \n" );
        String simbolos = compilador.tablaSimbolos();
        System.out.println(simbolos);

    }

}