const colors = require('tailwindcss/colors');

module.exports = {
  mode: 'jit',
  purge: {
    content: ["./public/index.html", "./src/**/*.cljs"],
  },
  darkMode: "class", // or 'media' or 'class'
  theme: {
    extend: {
      colors: {
        rose: colors.rose,
        orange: colors.orange,
        teal: colors.teal,
        emerald: colors.emerald,
        lightBlue: colors.lightBlue,
        cyan: colors.cyan,
        amber: colors.amber,
        lime: colors.lime,
        violet: colors.violet,
      },
      transitionProperty: {
        'width': 'width',
      },
    },
  },
  variants: {},
  plugins: [require("@tailwindcss/forms")],
};
