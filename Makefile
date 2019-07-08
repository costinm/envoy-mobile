
# Builder image for local run.
# TODO: add android SDK and NDK to the image (so they are cached and reproductible). Needed for running in CI
BUILD_IMAGE=envoyproxy/envoy-build:ec38ecb88fd1abe55ab1daa2f6bd239ffccc9d98

build:
	docker run -it --rm -v `pwd`:`pwd` -w `pwd` \
    -v ${ANDROID_HOME}:/Android -v ${ANDROID_NDK_HOME}:/AndroidNDK \
    -e ANDROID_HOME=/Android -e ANDROID_NDK_HOME=/AndroidNDK \
    --name envoy ${BUILD_IMAGE} bazel build android_dist --config=android
