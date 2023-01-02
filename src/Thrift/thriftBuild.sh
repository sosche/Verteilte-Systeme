#!/bin/bash
thrift -r --gen java ConsumerControl.thrift
thrift -r --gen java NuclearControl.thrift
thrift -r --gen java Print.thrift
thrift -r --gen java SolarControl.thrift
thrift -r --gen java WindControl.thrift
