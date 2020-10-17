import Vue from 'vue'
import FrontPage from '@/components/FrontPage'

describe('FrontPage.vue', () => {
  it('should render correct contents', () => {
    const Constructor = Vue.extend(FrontPage)
    const vm = new Constructor().$mount()
    expect(vm.$el.querySelector('.hero-body').textContent)
      .to.contain('Memo')
  })
})
