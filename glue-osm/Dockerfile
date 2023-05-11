FROM amazon/aws-glue-libs:glue_libs_4.0.0_image_01

ENV WORK_DIR=/home/glue_user/interactive-sessions

RUN mkdir -p $WORK_DIR

WORKDIR $WORK_DIR

RUN python3 -m venv .venv

ENV PATH="$WORK_DIR/.venv/bin:$PATH"

RUN pip3 install --upgrade jupyter boto3 aws-glue-sessions

RUN export SITE_PACKAGES=$(pip3 show aws-glue-sessions | grep Location | awk '{print $2}') \
  && jupyter kernelspec install --sys-prefix $SITE_PACKAGES/aws_glue_interactive_sessions_kernel/glue_pyspark \
  && jupyter kernelspec install --sys-prefix $SITE_PACKAGES/aws_glue_interactive_sessions_kernel/glue_spark