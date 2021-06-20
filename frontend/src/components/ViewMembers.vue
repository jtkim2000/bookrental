<template>

<v-data-table
    :headers="headers"
    :items="viewMembers"
    :items-per-page="5"
    class="elevation-1"
  ></v-data-table>

</template>

<script>
  const axios = require('axios').default;

  export default {
    name: 'ViewMembers',
    props: {
      value: Object,
      editMode: Boolean,
      isNew: Boolean
    },
    data: () => ({
        headers: [
            { text: "memberId", value: "memberId" },
            { text: "name", value: "name" },
            { text: "grade", value: "grade" },
            { text: "status", value: "status" },
        ],
        viewMembers : [],
    }),
    async created() {
      var temp = await axios.get(axios.backend + '/viewmembers')

      this.viewMembers = temp.data._embedded.viewmembers;

    },
    methods: {
    }
  }
</script>

