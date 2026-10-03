using System;

/* Programa de ejemplo
   para probar el analizador léxico */
class Programa
{
    static void Main(string[] args)
    {
        // definición de variables
        int edad = 25;
        double precio = 19.99;
        float tasa = 1.5f;
        string nombre = "Andrés";
        char inicial = 'A';
        bool activo = true;
        var total = precio * edad;

        if (edad >= 18 && activo)
        {
            Console.WriteLine($"Hola {nombre}\n");
        }

        for (int i = 0; i < 3; i++)
        {
            total += i;
        }

        int malo = 5 # 2;
    }
}
