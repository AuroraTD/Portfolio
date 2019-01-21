#!/bin/python

################################################################################################################
# FILE:                lake.py
#
# AUTHORS:             attiffan    Aurora T. Tiffany-Davis
#                      ssbehera    Subhendu S. Behera
#                      wpmoore2    Wade P. Moore
#
# DESCRIPTION:         Model the surface of a lake, where some pebbles have been thrown onto the surface.
#                      The energy level at any point on the lake is influenced by
#                          the energy level on that point in the past,
#                          and by the current energy levels at neighboring points.
#                      This program uses a 13-point stencil
#
# MODIFIED FROM:       https://pages.github.ncsu.edu/fmuelle/parsys18/hw/hw6/
#
# TO RUN:              srun -pgtx480 -N2 -n32 --pty /bin/bash
#                      pip2 install --user --no-cache-dir horovod==0.9.7
#                      mpirun -np 2 ./lake-horo.py [grid size] [number of pebbles] [iterations of the main loop]
################################################################################################################

#Import libraries for simulation
import tensorflow as tf
import numpy as np
import sys
import time

# Imports for visualization
import PIL.Image

#Import horovod
import horovod.tensorflow as hvd

hvd.init()

config = tf.ConfigProto()
config.gpu_options.allow_growth = True
config.gpu_options.visible_device_list = str(hvd.local_rank())

# Display function
def DisplayArray(a, fmt='jpeg', rng=[0,1], N=1):
  """Display an array as a picture."""
  step = float(1)/N
  with open("lake_c_" + str(hvd.rank()) + ".dat", 'w') as final_file:
      for i in range(N):
          for j in range(N):
              final_file.write("{:0.4f} {:0.4f} {:0.4f}\n".format(float(i)*step, float(j)*step, a[i][j]))


  a = (a - rng[0])/float(rng[1] - rng[0])*255
  a = np.uint8(np.clip(a, 0, 255))
  with open("lake_py_" + str(hvd.rank()) + ".jpg", "w") as f:
      PIL.Image.fromarray(a).save(f, "jpeg")

# Create interactive Tensorflow session
sess = tf.InteractiveSession()

# Computational Convenience Functions
def make_kernel(a):
  """Transform a 2D array into a convolution kernel"""
  a = np.asarray(a)
  a = a.reshape(list(a.shape) + [1,1])
  return tf.constant(a, dtype=1)

def simple_conv(x, k):
  """A simplified 2D convolution operation"""
  x = tf.expand_dims(tf.expand_dims(x, 0), -1)
  y = tf.nn.depthwise_conv2d(x, k, [1, 1, 1, 1], padding='SAME')
  return y[0, :, :, 0]

# Stencil definitions
def laplace(x):
  """Compute the 2D laplacian of an array"""

  five_point = [[0.0, 1.0, 0.0],
                [1.0, -4., 1.0],
                [0.0, 1.0, 0.0]]


  nine_point = [[0.25, 1.0, 0.25],
                [1.00, -5., 1.00],
                [0.25, 1.0, 0.25]]

  thirteen_point = [[0.0,   0.0,    0.0,    0.125,  0.0,    0.0,    0.0],
                    [0.0,   0.0,    0.0,    0.25,   0.0,    0.0,    0.0],
                    [0.0,   0.0,    0.0,    1.0,    0.0,    0.0,    0.0],
                    [0.125, 0.25,   1.0,    -5.5,   1.0,    0.25,   0.125],
                    [0.0,   0.0,    0.0,    1.0,    0.0,    0.0,    0.0],
                    [0.0,   0.0,    0.0,    0.25,   0.0,    0.0,    0.0],
                    [0.0,   0.0,    0.0,    0.125,  0.0,    0.0,    0.0]]

  laplace_k = make_kernel(thirteen_point)
  return simple_conv(x, laplace_k)

# Provide usage instructions
if len(sys.argv) != 4:
    print ("Usage:", sys.argv[0], "N num_pebbles num_iter")
    sys.exit()

# Save arguments
N = int(sys.argv[1])
num_pebbles = int(sys.argv[2])
num_iter = int(sys.argv[3])

# Initial Conditions -- some rain drops hit a pond

# Set everything to zero

energy_current_init  = np.zeros([N + 3, N], dtype=np.float32)
energy_new_init = np.zeros([N + 3, N], dtype=np.float32)

recv_energy_current_0  = np.zeros([3, N], dtype=np.float32)
recv_energy_new_0  = np.zeros([3, N], dtype=np.float32)
recv_energy_current_1  = np.zeros([3, N], dtype=np.float32)
recv_energy_new_1  = np.zeros([3, N], dtype=np.float32)

# Create tensorflow variables

recv_energy_current_buf_0  = tf.Variable(recv_energy_current_0)
recv_energy_new_buf_0  = tf.Variable(recv_energy_new_0)
recv_energy_current_buf_1  = tf.Variable(recv_energy_current_1)
recv_energy_new_buf_1  = tf.Variable(recv_energy_new_1)

send_energy_current  = np.zeros([3, N], dtype=np.float32)
send_energy_new  = np.zeros([3, N], dtype=np.float32)

send_energy_current_buf = tf.Variable(send_energy_current)
send_energy_new_buf = tf.Variable(send_energy_new)

# Some rain drops hit a pond at random points
for n in range(num_pebbles):
  a,b = np.random.randint(0, N, 2)
  if hvd.rank() == 0:
      energy_current_init[a,b] = np.random.uniform()
  else:
    energy_current_init[a + 3, b] = np.random.uniform()

# Parameters:
# eps -- time resolution
# damping -- wave damping
eps = tf.placeholder(tf.float32, shape=())
damping = tf.placeholder(tf.float32, shape=())

# Create variables for simulation state
energy_current  = tf.Variable(energy_current_init)
energy_new = tf.Variable(energy_new_init)

# Define operations on tensorflow variables

if hvd.rank() == 0:
    send_op = tf.group(
          tf.assign(send_energy_current_buf, energy_current[N-3:N,:]),
          tf.assign(send_energy_new_buf, energy_new[N-3:N,:]))
else:
    send_op = tf.group(
          tf.assign(send_energy_current_buf, energy_current[3:6,:]),
          tf.assign(send_energy_new_buf, energy_new[3:6,:]))

bcast = tf.group(
        tf.assign(recv_energy_current_buf_1, hvd.broadcast(send_energy_current_buf, 0)),
        tf.assign(recv_energy_current_buf_0, hvd.broadcast(send_energy_current_buf, 1)),
        tf.assign(recv_energy_new_buf_1, hvd.broadcast(send_energy_new_buf, 0)),
        tf.assign(recv_energy_new_buf_0, hvd.broadcast(send_energy_new_buf, 1)));

if hvd.rank() == 0:
    recv_op = tf.group(
                tf.scatter_update(energy_current, [N, N + 1, N+2], recv_energy_current_buf_0[0:3, :]),
                tf.scatter_update(energy_new, [N, N + 1, N+2], recv_energy_new_buf_0[0:3, :]))
else:
    recv_op = tf.group(
                tf.scatter_update(energy_current, [0, 1, 2], recv_energy_current_buf_1[0:3,:]),
                tf.scatter_update(energy_new, [0, 1, 2], recv_energy_new_buf_1[0:3,:]))

# Discretized PDE update rules
energy_old = energy_current + eps * energy_new
energy_other = energy_new + eps * (laplace(energy_current) - damping * energy_new)

# Operation to update the state
step = tf.group(
  energy_current.assign(energy_old),
  energy_new.assign(energy_other))

# Initialize state to initial conditions
tf.global_variables_initializer().run()

# Run num_iter steps of PDE
start = time.time()
for i in range(num_iter):
  if i in [100, 200, 300]:
    if hvd.rank() == 0:
      print ("Iteration: {}..".format(i))
  # Horovod data transfer
  send_op.run()
  bcast.run()
  recv_op.run()
  step.run({eps: 0.06, damping: 0.03})

# Finish
end = time.time()
print('Elapsed time: {} seconds'.format(end - start))
DisplayArray(energy_current.eval(), rng=[-0.1, 0.1], N=N)
