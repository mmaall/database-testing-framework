#/bin/bash


sudo apt-get update
sudo apt-get -y install g++ cmake libbz2-dev libaio-dev bison \
zlib1g-dev libsnappy-dev libboost-all-dev
sudo apt-get -y install libgflags-dev libreadline6-dev libncurses5-dev \
libssl-dev liblz4-dev gdb git

sudo apt-get -y install libzstd1 libzstd1-dev

sudo ln -s /usr/lib/x86_64-linux-gnu/libz.so /usr/lib/libz.so

export CFLAGS="-Wno-implicit-fallthrough -Wno-int-in-bool-context \
  -Wno-shift-negative-value -Wno-misleading-indentation \
  -Wno-format-overflow -Wno-nonnull -Wno-unused-function"

export CXXFLAGS="-Wno-implicit-fallthrough -Wno-int-in-bool-context \
  -Wno-shift-negative-value -Wno-misleading-indentation \
  -Wno-format-overflow -Wno-nonnull -Wno-unused-function \
  -Wno-aligned-new"


git clone https://github.com/facebook/mysql-5.6.git
cd mysql-5.6
git submodule init
git submodule update
