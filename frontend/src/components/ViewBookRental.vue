<template>

<v-data-table
    :headers="headers"
    :items="viewBookRental"
    :items-per-page="5"
    class="elevation-1"
  ></v-data-table>

</template>

<script>
  const axios = require('axios').default;

  export default {
    name: 'ViewBookRental',
    props: {
      value: Object,
      editMode: Boolean,
      isNew: Boolean
    },
    data: () => ({
        headers: [
            { text: "rentalId", value: "rentalId" },
            { text: "bookId", value: "bookId" },
            { text: "qty", value: "qty" },
            { text: "memberId", value: "memberId" },
            { text: "bookStatus", value: "bookStatus" },
            { text: "status", value: "status" },
        ],
        viewBookRental : [],
    }),
    async created() {
      var temp = await axios.get(axios.backend + '/viewbookrentals')

      this.viewBookRental = temp.data._embedded.viewbookrentals;

    },
    methods: {
    }
  }
</script>

