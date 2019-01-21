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
# TO RUN:              srun -pgtx480 -n16 --pty /bin/bash
#                      pip2 install --user Pillow
#                      ./lake.py [grid size] [number of pebbles] [iterations of the main loop]
#
# TO COMPARE:          srun -pgtx480 -n16 --pty /bin/bash
#                      ./lake.o [grid size] [number of pebbles] [iterations of the main loop]
################################################################################################################

#Import libraries for simulation
import tensorflow as tf
import numpy as np
import sys
import time

# Imports for visualization
import PIL.Image

# Display function
def DisplayArray(a, fmt='jpeg', rng=[0,1], N=0):
  """Display an array as a picture."""
  step = float(1/N)
  with open('lake_c.dat', 'w') as final_file:
      for i in range(N):
          for j in range(N):
              final_file.write("{:0.4f} {:0.4f} {:0.4f}\n".format(float(i*step), float(j*step), a[i][j]))


  a = (a - rng[0])/float(rng[1] - rng[0])*255
  a = np.uint8(np.clip(a, 0, 255))
  with open("lake_py.jpg","w") as f:
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
	print ("Usage:", sys.argv[0], "N npebs num_iter")
	sys.exit()

# Save arguments
N = int(sys.argv[1])
npebs = int(sys.argv[2])
num_iter = int(sys.argv[3])

# Initial Conditions -- some rain drops hit a pond

# Set everything to zero
u_init  = np.zeros([N, N], dtype=np.float32)
ut_init = np.zeros([N, N], dtype=np.float32)

# Some rain drops hit a pond at random points
for n in range(npebs):
  a,b = np.random.randint(0, N, 2)
  u_init[a,b] = np.random.uniform()

# Parameters:
# eps -- time resolution
# damping -- wave damping
eps = tf.placeholder(tf.float32, shape=())
damping = tf.placeholder(tf.float32, shape=())

# Create variables for simulation state
U  = tf.Variable(u_init)
Ut = tf.Variable(ut_init)

# Discretized PDE update rules
U_ = U + eps * Ut
Ut_ = Ut + eps * (laplace(U) - damping * Ut)

# Operation to update the state
step = tf.group(
  U.assign(U_),
  Ut.assign(Ut_))

# Initialize state to initial conditions
tf.global_variables_initializer().run()

# Run num_iter steps of PDE
start = time.time()
for i in range(num_iter):
  # Step simulation
  step.run({eps: 0.06, damping: 0.03})

# Finish
end = time.time()
print('Elapsed time: {} seconds'.format(end - start))
DisplayArray(U.eval(), rng=[-0.1, 0.1], N=N)
