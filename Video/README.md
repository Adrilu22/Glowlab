Link video :
https://drive.google.com/file/d/17lZbRy_J0WlUNxwrAE1WUreC_PmMcUtE/view?usp=sharing

Durante el proceso de despliegue de la aplicación, se presentó un inconveniente en el funcionamiento del botón “Finalizar compra”, el cual no respondía al hacer clic.

 Causa del problema:
El error se debía a que Firebase Hosting no había actualizado correctamente los cambios del frontend, por lo que la aplicación estaba ejecutando una versión anterior del código.

Solución aplicada:
Se realizó nuevamente el despliegue desde la terminal de Visual Studio Code utilizando el siguiente comando:

npx firebase deploy


Para mantener un control adecuado de los cambios realizados en el proyecto, se utilizó Git y GitHub siguiendo este flujo de trabajo:

Verificar el estado de los archivos:
git status
Agregar los cambios realizados:
git add .
Crear un commit con un mensaje descriptivo:
git commit -m "Descripción del cambio realizado"
Subir los cambios al repositorio remoto:
git push origin main


