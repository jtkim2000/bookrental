<template>

<v-data-table
    :headers="headers"
    :items="viewBookInfo"
    :items-per-page="5"
    class="elevation-1"
  ></v-data-table>

</template>

<script>
  const axios = require('axios').default;

  export default {
    name: 'ViewBookInfo',
    props: {
      value: Object,
      editMode: Boolean,
      isNew: Boolean
    },
    data: () => ({
        headers: [
            { text: "bookId", value: "bookId" },
            { text: "title", value: "title" },
            { text: "qty", value: "qty" },
            { text: "status", value: "status" },
        ],
        viewBookInfo : [],
    }),
    async created() {
      var temp = await axios.get(axios.backend + '/viewbookinfos')

      this.viewBookInfo = temp.data._embedded.viewbookinfos;

    },
    methods: {
    }
  }
</script>

