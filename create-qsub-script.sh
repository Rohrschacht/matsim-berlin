#!/usr/bin/env bash

# print a qsub script file based on env, git email or defaults

echo "#!/bin/bash --login
#$ -cwd
#$ -N ${RUN_ID:=matsim-example}
#$ -m be
#$ -M $(git config user.email)
#$ -j y
#$ -o ${JOB_LOG:=$RUN_ID.log}
#$ -l h_rt=80000
#$ -l mem_free=8G
#$ -pe mp 8

# make sure java is present
module add java/${JAVA_VERSION:=11}

# make sure the output directory exists
mkdir --parents ${OUTPUT_DIR:=./output/$RUN_ID}

# log version for debug purposes
java -version

# start matsim
java --class-path ${JAR:=./matsim-berlin.jar} \
  -Xmx64G '${MAIN_CLASS:=org.matsim.run.RunBerlinScenario}' \
  ${CONFIG:=./input/config.xml} \
  '--config:controler.runId' ${RUN_ID} \
  '--config:controler.outputDirectory' ${OUTPUT_DIR}
"
