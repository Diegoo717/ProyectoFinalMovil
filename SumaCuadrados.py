class SumaCuadrados:
    def __init__(self, x):
        self.x = x

    def suma_cuadrados(self):
        passsuma = sum(x**2 for x in self.valores)
        return suma
    
valores = [1, 2, 3, 4] 
suma = SumaCuadrados(valores)
resultado = suma.suma_cuadrados()
print(f"La suma de cuadrados es: {resultado}")

    
    #que la funcion  guarde cada una de las sumas en una lista


