NAME=matsim-berlin-run
REMOTE_CONFIG=./config/berlin-v5.5-1pct.config.xml
LOCAL_CONFIG=./scenarios/berlin-v5.5-1pct/input/berlin-v5.5-1pct.config.xml
# name of the artifacts generated by IntellIJ
LOCAL_NAME=matsim-berlin
ARTIFACTS_DIR=out/artifacts/
QSUB_SCRIPT="out/${NAME}.sh"

# setup your .ssh/config with the proper username and credentials (password or ssh-key)
REMOTE=cluster.math.tu-berlin.de
# path on remote to matsim-class folder or symlink `ln -s /net/ils/matsim-class/<folder> matsim-class`
REMOTE_FOLDER_ROOT=matsim-class

#build:
#	mvn package -DskipTests

# script task env vars
script%: export RUN_ID=${NAME}
script%: export JOB_LOG=${NAME}.log
script%: export JAVA_VERSION=11
script%: export JAR="artifacts/${NAME}/*:artifacts/${NAME}.jar"
script%: export CONFIG=${REMOTE_CONFIG}
script%: export MAIN_CLASS=org.matsim.run.RunBerlinScenario

# print how the qsub script would be generated
script-echo:
	./create-qsub-script.sh

script-build:
	./create-qsub-script.sh > ${QSUB_SCRIPT}
	chmod +x ${QSUB_SCRIPT}

# rsync JAR
# (if jar is too big, maybe explode the jar and sync the exploded files (should be less when repeated, but requires setup on how to run and maybe some config in mvn)
sync: script-build
	@ssh ${REMOTE} -t test -L ${REMOTE_FOLDER_ROOT} || (echo "The remote folder ${REMOTE_FOLDER_ROOT} does not exist on ${REMOTE}. Did you symlink your folder with 'ln -s /net/ils/matsim-class/<folder> ${REMOTE_FOLDER_ROOT}'?" && exit 1)
	ssh ${REMOTE} -t mkdir --parents ${REMOTE_FOLDER_ROOT}/config ${REMOTE_FOLDER_ROOT}/artifacts/${NAME}
	rsync --update --human-readable --partial --progress ${QSUB_SCRIPT} ${REMOTE}:${REMOTE_FOLDER_ROOT}/${NAME}.sh
	rsync --update --human-readable --partial --progress ${LOCAL_CONFIG} ${REMOTE}:${REMOTE_FOLDER_ROOT}/${REMOTE_CONFIG}
	rsync --recursive --update --human-readable --partial --progress ${ARTIFACTS_DIR} ${REMOTE}:${REMOTE_FOLDER_ROOT}/artifacts/main
	ssh ${REMOTE} -t rsync --recursive --update --human-readable --partial --progress ${REMOTE_FOLDER_ROOT}/artifacts/main/${LOCAL_NAME} ${REMOTE_FOLDER_ROOT}/artifacts/${NAME}
	ssh ${REMOTE} -t rsync --update --human-readable --partial --progress ${REMOTE_FOLDER_ROOT}/artifacts/main/${LOCAL_NAME}.jar ${REMOTE_FOLDER_ROOT}/artifacts/${NAME}.jar

# create and deploy qsub script; enqueue qsub script
deploy: sync
	ssh ${REMOTE} -t "cd ${REMOTE_FOLDER_ROOT} && qsub ${NAME}.sh"

log:
	ssh ${REMOTE} -t "test -f ${REMOTE_FOLDER_ROOT}/${NAME}.log || touch ${REMOTE_FOLDER_ROOT}/${NAME}.log && tail -f ${REMOTE_FOLDER_ROOT}/${NAME}.log"


.PHONY: script-echo script-build sync deploy log