**Create** **a new virtual environment using your preferred Python 3.14:**

bash

```
python3.14 -m venv venv_314
```

**Activate** **the environment:**bash

```
source venv_314/bin/activate
```

Install dependencies in your local environment

```
pip install -r requirements.txt
```



Start Your PostgreSQL Server

bash

```
brew services start postgresql
```


**Run the training script *

bash

```
python3.14 train_model.py
```




Build the Docker Image

Navigate to your Python project directory in your terminal and run the Docker build command. This command uses your `Dockerfile` to create an image named `customer-segmentation-service`:

bash

```
docker build . -t customer-segmentation-service:latest
```

**Verify app.py loaded the model**

bash

```
docker logs segmentation_app
```
