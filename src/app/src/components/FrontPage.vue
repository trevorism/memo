<template>
  <div id="splash" class="container">
    <section class="hero is-link">
      <div class="hero-body">
        <div class="container">
          <h1 class="title has-text-centered">
            Memo: Send us a message
          </h1>
        </div>
      </div>
    </section>
    <section class="section glassBackground">
      <div class="container">
        <div class="is-centered has-text-centered">
          <div class="is-size-4 has-text-weight-semibold">
            Tell us what's on your mind.
          </div>
        </div>
        <div class="is-centered has-text-centered">
          <ul>
            <li class="">100% Anonymous and Confidential</li>
          </ul>
        </div>
      </div>
    </section>
    <section class="section puzzleBackground mb-6">
      <div class="columns">
        <div class="column is-one-half borderRight">
          <div class="title has-text-centered">
            Text
          </div>
          <div class="has-text-centered">
            <b-button tag="router-link" class="is-info is-large" :rounded="true" to="/text">Go!</b-button>
          </div>
        </div>
        <div class="column is-one-half borderLeft">
          <div class="title has-text-centered">
            Audio
          </div>
          <div class="has-text-centered">
            <b-button tag="router-link" class="is-info-passive is-large" :rounded="true" to="/">Coming Soon...</b-button>
          </div>
        </div>
      </div>
    </section>
    <section>
      <div class="container is-centered has-text-centered">
        Powered by <a href="https://trevorism.com">Trevorism</a>
      </div>
    </section>
  </div>
</template>

<script>

export default {
  name: 'FrontPage'
}
</script>

<style scoped>
.glassBackground {
  background-color: #a7fad9;
}
.puzzleBackground {
  background-color: #c8b53f;
}

</style>
