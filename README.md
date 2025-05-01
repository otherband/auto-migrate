#### You have just published an open-source library.

##### It is an immediate hit. Congratulations!

You hit the ground running, the stars are piling up,
and your work is helping thousands of developers.
Unfortunately, you like to refactor your code often,
and in your eagerness to get your work off the ground,
you've misnamed quite a few of the classes and functions that make up your public API.
You don't want to introduce too many breaking changes into your work already...

But what if you could?

With `auto-migrate`, you can build a script that'll automate the migration for your users.
A bot similar to `dependabot` will pick up on this, and create a merge request with the desired changes.
Viola! You're free to refactor your code, and the resultant API changes will
be automatically reflected to your users' repos.