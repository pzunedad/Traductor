package Practica_5;

import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

public class AnalizadorSintactico {

    private Lexico lexico;

    private ComponenteLexico componenteLexico;
    private Hashtable<String,String> simbolos;
    private String tipo;
    private int tamano;
    public float tamano2;
    private boolean correcto = true;//saber si el programa se ha compilado correctamente
    protected boolean qTipo = true,Vec=false,comprSem=true,comprSem2=true;
    private String tipo1="",tipo2="";

    public String getTipo1(){
        return tipo1;
    }
    public String getTipo2(){
        return tipo2;
    }
    public void setTipo(String _tipo){
        if(!qTipo) {
            tipo1 = _tipo;
        } else {
            tipo2 = _tipo;
        }
    }
    public boolean getCorrecto(){
        return correcto;
    }
    public void setCorrecto(boolean b){
        correcto = b;
    }

    //AnalizadorSintactico inicializa el analizador lexico, el componente lexico y la tabla hash
    public AnalizadorSintactico(Lexico lexico) {
        this.lexico = lexico;
        this.componenteLexico = this.lexico.getComponenteLexico();
        this.simbolos = new Hashtable<String,String>();
    }

    //analisisSintactico comprueba que se cumple la estructura void main {}
    public void analisisSintactico() {
        compara("void");
        compara("main");
        compara("open_bracket");
        declaraciones();
        instrucciones();
        compara("closed_bracket");
    }

    //instrucciones comprueba si el token tiene como etiqueta alguna de estas palabras reservadas, si las hay, se vuelve a llamar a esta funcion en caso de que haya mas
    public void instrucciones(){
        if(this.componenteLexico.getEtiqueta().equals("int")||
                this.componenteLexico.getEtiqueta().equals("float")||
                this.componenteLexico.getEtiqueta().equals("boolean") ||
                this.componenteLexico.getEtiqueta().equals("id")||
                this.componenteLexico.getEtiqueta().equals("if")||
                this.componenteLexico.getEtiqueta().equals("while")||
                this.componenteLexico.getEtiqueta().equals("do") ||
                this.componenteLexico.getEtiqueta().equals("print") ||
                this.componenteLexico.getEtiqueta().equals("double_slash") ||
                this.componenteLexico.getEtiqueta().equals("divide") ||
                this.componenteLexico.getEtiqueta().equals("open_bracket")){
            instruccion();
            instrucciones();
        }
    }

    //intruccion comprueba segun la palabra reservada que haya, si la estructura de esa instruccion es correcta
    public void instruccion(){
        switch (this.componenteLexico.getEtiqueta()) {
            case "int", "float", "boolean" -> declaracionVariables();
            case "id" -> {

                variable();
                compara("assignment");
                expresionLogica();
                compara("semicolon");
            }
            case "if" -> {
                compara("if");
                compara("open_parenthesis");
                expresionLogica();
                compara("closed_parenthesis");
                instruccion();
                if (this.componenteLexico.getEtiqueta().equals("else")) {
                    compara("else");
                    instruccion();
                }
            }
            case "while" -> {
                compara("while");
                compara("open_parenthesis");
                expresionLogica();
                compara("closed_parenthesis");
                instruccion();
            }
            case "do" -> {
                compara("do");
                instruccion();
                compara("while");
                compara("open_parenthesis");
                expresionLogica();
                compara("closed_parenthesis");
                compara("semicolon");
            }
            case "print" -> {
                compara("print");
                compara("open_parenthesis");
                variable();
                compara("closed_parenthesis");
                compara("semicolon");
            }
            case "open_bracket" -> {
                compara("open_bracket");
                instrucciones();
                compara("closed_bracket");
            }

            case "divide" ->{
                if(this.lexico.inicioComL()) {
                    this.lexico.comentariosLargos();
                    this.componenteLexico = this.lexico.getComponenteLexico();
                }

            }
            case "double_slash" ->{
                compara("double_slash");
                this.lexico.comentariosCortos();
                this.componenteLexico = this.lexico.getComponenteLexico();

            }


            default -> System.out.println("Instruccion invalida");
        }
    }

    //declaraciones comprueba si la etiqueta del token es un tipo de dato, se llama al final por si hubiese mas
    public void declaraciones() {
        if(this.componenteLexico.getEtiqueta().equals("int") ||
                this.componenteLexico.getEtiqueta().equals("float") ||
                this.componenteLexico.getEtiqueta().equals("boolean")) {
            declaracionVariables();
            declaraciones();
        }

        //if(this.lexico.checkComments()){this.componenteLexico = this.lexico.getComponenteLexico();}
    }

    //declaracionVariables determina si estoy declarando un array o un identificador normal
    public void declaracionVariables() {
        tipoPrimitivo();
        if (this.componenteLexico.getEtiqueta().equals("open_square_bracket")){
            tipoVector();
            if(estaEnTablaid(this.componenteLexico.getValor())) {
                System.out.println("Error en linea "+this.lexico.getLineas()+", la variable "+this.componenteLexico.getValor()+" ya esta declarada\n\n");
                setCorrecto(false);
            }
                simbolos.put(this.componenteLexico.getValor(), tipo);
                compara("id");
                compara("semicolon");

        } else{
            listaIdentificadores();
            compara("semicolon");
        }

    }

    //variable se utiliza para comprobar si hay un id, o si hay un array(se usa para la asignacion y comprobar estructuras)
    public void variable(){
        //esta en la tabla
        if(!estaEnTablaid(this.componenteLexico.getValor())) {//primero decimos si la variable esta en la tabla
            System.out.println("Error en linea "+this.lexico.getLineas()+", la variable "+this.componenteLexico.getValor()+" no esta declarada");
            setCorrecto(false);
        }
        //comprobamos el tipo de dato
        Vec = this.lexico.esVector(Vec);
        if(!Vec&&comprSem) {
            qTipo = !qTipo;//empezamos en false pos1, el siguiente es true pos 2
            setTipo(this.componenteLexico.getValor());
            if (tipo1 != "" && tipo2 != "" && estaEnTablaid(tipo1) && estaEnTablaid(tipo2)) {//comprobacion semantica
                if (!estaEnTablaTipo(tipo1, tipo2)) {
                    System.out.println("Error en linea " + this.lexico.getLineas() + ", los tipos de variables introducidos no son compatibles");
                    setCorrecto(false);
                    comprSem=false;
                    setTipo("");//nuevo tipo2
                    qTipo = !qTipo;//pos1 false
                } else {
                    setTipo("");//nuevo tipo2
                    qTipo = !qTipo;//pos1 false
                }
            }
        }   //parte original
            if (this.componenteLexico.getEtiqueta().equals("id")) {
                compara("id");
                if (this.componenteLexico.getEtiqueta().equals("open_square_bracket")) {
                    compara("open_square_bracket");
                    expresion();
                    compara("closed_square_bracket");
                }
            }
    }

    //tipoVector determina si la estructura de un array es correcta y prepara un tipo de dato mas personalizado(array(int,x))
    public void tipoVector(){
        compara("open_square_bracket");
        tamano = Integer.parseInt(this.componenteLexico.getValor());
        tipo = "array(" + tipo + "," + tamano + ")";
        this.componenteLexico = this.lexico.getComponenteLexico();
        compara("closed_square_bracket");
    }

    //tipoPrimitivo comprueba que un id, tenga un tipo asignado
    public void tipoPrimitivo() {
        if (this.componenteLexico.getEtiqueta().equals("int") ||
                this.componenteLexico.getEtiqueta().equals("float") ||
                this.componenteLexico.getEtiqueta().equals("boolean")) {
            this.tipo = this.componenteLexico.getEtiqueta();
            this.componenteLexico = this.lexico.getComponenteLexico();

        } else {
            System.out.println("Expected: int or float or boolean");
        }
    }

    //listaIdentificadores guarda los distintos id en la tabla hash
    public void listaIdentificadores() {
        String aux;
        if(estaEnTablaid(this.componenteLexico.getValor())) {
            System.out.println("Error en la linea "+this.lexico.getLineas()+", la variable "+this.componenteLexico.getValor()+" ya esta declarada");
            setCorrecto(false);
        }
            simbolos.put(this.componenteLexico.getValor(), tipo);
            aux=this.componenteLexico.getValor();
            compara("id");
            if(this.componenteLexico.getEtiqueta().equals("assignment")&&comprSem2){
                qTipo=false;
                setTipo(aux);
                comprSem2=false;
            }
            asignacionDeclarion();
            masIdentificadores();
    }

    //asignacionDeclarion comprueba que la expresion de asignacion es correcta
    public void asignacionDeclarion(){
        if (this.componenteLexico.getEtiqueta().equals("assignment")) {
            compara("assignment");
            expresionLogica();
        }
    }

    //masIdentificadores se usa cuando se declara mas de un id, seguido de comas
    public void masIdentificadores() {
        String aux;
        //Para leer las comas
        if (this.componenteLexico.getEtiqueta().equals("comma")) {
            compara("comma");
            if(estaEnTablaid(this.componenteLexico.getValor())) {
                System.out.println("Error en la linea "+this.lexico.getLineas()+", la variable "+this.componenteLexico.getValor()+" ya esta declarada");
                setCorrecto(false);
            }
                simbolos.put(this.componenteLexico.getValor(), tipo);
                compara("id");
                asignacionDeclarion();
                masIdentificadores();
        }
    }

    //expresionLogica era recursiva, quitando la recursividad queda otra regla que se llama expresionLogica_
    public void expresionLogica(){
        terminoLogico();
        expresionLogica_();
    }

    //expresionLogica_ comprueba con la etiqueta del componente lexico si hay un or ||,se llama a si mismo por si hay mas
    public void expresionLogica_(){
        if(this.componenteLexico.getEtiqueta().equals("or")){
            compara("or");
            terminoLogico();
            expresionLogica_();
        }
    }

    //terminoLogico era recursiva, quitando la recursividad queda otra regla que se llama terminoLogico_
    public void terminoLogico(){
        factorLogico();
        terminoLogico_();
    }

    //terminoLogico_ comprueba con la etiqueta del componente lexico si hay un and &&,se llama a si mismo por si hay mas
    public void terminoLogico_(){
        if(this.componenteLexico.getEtiqueta().equals("and")) {
            compara("and");
            factorLogico();
            terminoLogico_();
        }
    }

    //factorLogico comprueba si la etiqueta es un not, true o false;
    public void factorLogico(){
        switch (this.componenteLexico.getEtiqueta()) {
            case "not" -> {
                compara("not");
                factorLogico();
            }
            case "true" -> compara("true");
            case "false" -> compara("false");
            default -> expresionRelacional();
        }
    }

    //expresionRelacional determina si hay distintos operadores
    public void expresionRelacional(){
        expresion();
        if(this.componenteLexico.getEtiqueta().equals("less_than")||
                this.componenteLexico.getEtiqueta().equals("less_equals")||
                this.componenteLexico.getEtiqueta().equals("greater_than") ||
                this.componenteLexico.getEtiqueta().equals("greater_equals")||
                this.componenteLexico.getEtiqueta().equals("equals")||
                this.componenteLexico.getEtiqueta().equals("not_equals")){
            operadorRelacional();
            expresion();
        }
    }

    //expresion era recursiva, quitando la recursividad queda otra regla que se llama expresion_
    public void expresion(){
        termino();
        expresion_();
    }

    //expresion_ determina si hay una suma o una resta,se llama a si mismo por si hay mas
    public void expresion_(){
        if(this.componenteLexico.getEtiqueta().equals("add")) {
            compara("add");
            termino();
            expresion_();
        } else if (this.componenteLexico.getEtiqueta().equals("subtract")) {
            compara("subtract");
            termino();
            expresion_();
        }
    }

    //termino era recursiva, quitando la recursividad queda otra regla que se llama termino_
    public void termino(){
        factor();
        termino_();
    }

    //termino_ determina si hay multiplicaciones, divisiones o remainder,se llama a si mismo por si hay mas
    public void termino_(){
        switch (this.componenteLexico.getEtiqueta()) {
            case "multiply" -> {
                compara("multiply");
                factor();
                termino_();
            }
            case "divide" -> {
                compara("divide");
                factor();
                termino_();
            }
            case "remainder" -> {
                compara("remainder");
                factor();
                termino_();
            }
        }
    }

    //factor se divide en (expresion),variable, numero
    public void factor(){
        if(this.componenteLexico.getEtiqueta().equals("open_parenthesis")) {
            compara("open_parenthesis");
            expresion();
            compara("closed_parenthesis");
        } else if (this.componenteLexico.getEtiqueta().equals("id")) {
            variable();
        }else{
            if(!this.componenteLexico.getEtiqueta().equals("float")) {
                tamano = Integer.parseInt(this.componenteLexico.getValor());
            }
            else {
                tamano2 = Float.parseFloat(this.componenteLexico.getValor());
            }
            this.componenteLexico = this.lexico.getComponenteLexico();
        }
    }

    //operadorRelacional determina si las palabras reservadas estan bien escritas en el codigo
    public void operadorRelacional(){
        switch (this.componenteLexico.getEtiqueta()) {
            case "less_than" -> compara("less_than");
            case "less_equals" -> compara("less_equals");
            case "greater_than" -> compara("greater_than");
            case "greater_equals" -> compara("greater_equals");
            case "equals" -> compara("equals");
            case "not_equals" -> compara("not_equals");
        }
    }

    //compara determina si la etiqueta del componente lexico coincide con lo que esta escrito(se utiliza para comprobar una estructura o si hay una palabra en especifico)
    public void compara(String token) {
        if(this.componenteLexico.getEtiqueta().equals(token)) {
            if(this.componenteLexico.getEtiqueta().equals("semicolon")){
                qTipo=false;
                setTipo("");
                qTipo=true;
                comprSem=true;
                comprSem2 = true;
            }
            this.componenteLexico = this.lexico.getComponenteLexico();
        }else {
            System.out.println("Expected: " + token + " en linea "+ (lexico.getLineas()));
            System.out.println(this.componenteLexico.getEtiqueta());
        }
    }

    //tablaSimbolos imprime la tabla de simbolos
    public String tablaSimbolos() {
        String simbolos = "";
        Set<Map.Entry<String, String>> s = this.simbolos.entrySet();
        if(s.isEmpty()) System.out.println("La tabla de simbolos esta vacia\n");
        for(Map.Entry<String, String> m : s) {
            simbolos = simbolos + "<'" + m.getKey() + "', " +
                    m.getValue() + "> \n";
        }
        return simbolos;
    }

    //estaEnTablaid determina si un id esta ya en la tabla de simbolos
    public boolean estaEnTablaid(String palabra){//comprueba si un valor determinado esta en la tabla(para los nombres de los identificadores)
        boolean compr = false;
        Set<Map.Entry<String, String>> s = this.simbolos.entrySet();
        if(s.isEmpty()){
            return false;
        }
        for(Map.Entry<String, String> m : s) {
            if(m.getKey().equals(palabra)){
                compr=true;
                break;
            }
        }
        if(compr){
            return true;
        }
        else
        {
            return false;
        }
    }
    //estaEnTablaTipo detecta si 2 distintos id tienen el mismo tipo de dato
    public boolean estaEnTablaTipo(String palabra1,String palabra2){
        boolean compr = false;
        String tipo1="",tipo2="";
        Set<Map.Entry<String, String>> s = this.simbolos.entrySet();
        if(s.isEmpty()){
            return false;
        }
        for(Map.Entry<String, String> m : s) {
            if(m.getKey().equals(palabra1)){
                tipo1 = m.getValue();
                break;
            }
        }
        for(Map.Entry<String, String> m : s){
            if(m.getKey().equals(palabra2)){
                tipo2 = m.getValue();
                break;
            }
        }
        if(tipo1.equals(tipo2)){
            return true;
        }
        else{
            return false;
        }
    }
}
