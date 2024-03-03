package Practica_5;

public class ComponenteLexico{
    private String etiqueta; // etiqueta del token
    private String valor; // valor asociado a un token id o num

    public ComponenteLexico(String etiqueta) {
        this.etiqueta = etiqueta;
        this.valor = "";
    }
    public ComponenteLexico(String etiqueta, String valor) {
        this.etiqueta = etiqueta;
        this.valor = valor;
    }
    public String getEtiqueta() {
        return this.etiqueta;
    }

    public String getValor() {
        return this.valor;
    }
    // toString() devuelve una cadena con el contenido del token
    public String toString() {
        if (this.valor.length() == 0)
            return this.etiqueta;
        else
            return this.etiqueta + ", " + this.valor;
    }
}