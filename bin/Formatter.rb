# Results formatter
module Formatter
  # Base abstract class for results formatting.
  class Formatter
    # Takes an output stream and a print width.
    def initialize(o, w = 80)
      raise TypeError.new("Type %s of parameter %s is not IO" % [o.class, o]) \
        unless o.instance_of? IO
      @out = o
      @maxWidth = w
      @rowCount = 0
    end

    attr_reader :rowCount

    def header(args = [])
      row(args, false) if args.length > 0
      @rowCount = 0
    end
    
    # Output a row.
    # Inset is whether or not to offset row by a space.
    def row(args = [], inset = true)
      if not args or args.length == 0
        # Print out nothing
        return
      end
      if args.class == String
        output(@maxWidth, args)
        puts
        return
      end
      # TODO: Look at the type.  Is it RowResult?
      if args.length == 1
        splits = split(@maxWidth, dump(args[0]))
        for l in splits
          output(@maxWidth, l)
          puts
        end
      elsif args.length == 2
        col1width = 8
        col2width = 70
        splits1 = split(col1width, dump(args[0]))
        splits2 = split(col2width, dump(args[1]))
        biggest = (splits2.length > splits1.length)? splits2.length: splits1.length
        index = 0
        while index < biggest
          if inset
            # Inset by one space if inset is set.
            @out.print(" ")
          end
          output(col1width, splits1[index])
          if not inset
            # Add extra space so second column lines up w/ second column output
            @out.print(" ")
          end
          @out.print(" ")
          output(col2width, splits2[index])
          index += 1
          puts
        end
      else
        # Print a space to set off multi-column rows
        print ' '
        first = true
        for e in args
          @out.print " " unless first
          first = false
          @out.print e
        end
        puts
      end
      @rowCount += 1
    end

    def split(width, str)
      result = []
      index = 0
      while index < str.length do
        result << str.slice(index, index + width)
        index += width
      end
      result
    end

    def dump(str)
      # Remove double-quotes added by 'dump'.
      if str.instance_of? Fixnum
          return
      end
      return str.dump.slice(1, str.length)
    end

    def output(width, str)
      # Make up a spec for printf
      spec = "%%-%d.%ds" % [width, width]
      @out.printf(spec % str)
    end

    def footer(startTime = nil)
      if not startTime
        return
      end
      # Only output elapsed time and row count if startTime passed
      @out.puts("%d row(s) in %.4f seconds" % [@rowCount, Time.now - startTime])
    end
  end
     

  class Console < Formatter
  end

  class XHTMLFormatter < Formatter
    # http://www.germane-software.com/software/rexml/doc/classes/REXML/Document.html
    # http://www.crummy.com/writing/RubyCookbook/test_results/75942.html
  end

  class JSON < Formatter
  end

  # Do a bit of testing.
  if $0 == __FILE__
    formatter = Console.new(STDOUT)
    formatter.header(['a', 'b'])
    formatter.row(['a', 'b'])
    formatter.row(['xxxxxxxxx xxxxxxxxxxx xxxxxxxxxxx xxxxxxxxxxxx xxxxxxxxx xxxxxxxxxxxx xxxxxxxxxxxxxxx xxxxxxxxx xxxxxxxxxxxxxx'])
    formatter.row(['yyyyyy yyyyyy yyyyy yyy', 'xxxxxxxxx xxxxxxxxxxx xxxxxxxxxxx xxxxxxxxxxxx xxxxxxxxx xxxxxxxxxxxx xxxxxxxxxxxxxxx xxxxxxxxx xxxxxxxxxxxxxx  xxx xx x xx xxx xx xx xx x xx x x xxx x x xxx x x xx x x x x x x xx '])
    formatter.footer()
  end
end


