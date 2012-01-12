These are the known issues in the sequence diagram viewer.

Can't rename root of diagram when retrieving:
can't send a message before the diagram is loaded and then it causes everything, including IDAPro to freeze. Freezes also if we try to set the root activation's lifeline's name.

Unwanted navigation events:
no way to stop them. only thing could do is see which is active in eclipse, and ignore if navigation not on top

Breakpoint exception when tracing library functions:
apparently works on some machines?

State does not save renamed functions:
only can save to trace file, it's too complicated when finding everything when renamed.

Dynamic/navigation editors missing package viewer, breadcrumb viewer and right click options:
need to write content and label providers, and the package diagram???? dunno

Can't reload expanded when retrieving names:
causes freezes because too many messages at once on expand and retrieving names
