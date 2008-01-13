begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|thrift
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|UnsupportedEncodingException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|charset
operator|.
name|CharacterCodingException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|charset
operator|.
name|Charset
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|charset
operator|.
name|CharsetDecoder
import|;
end_import

begin_import
import|import
name|java
operator|.
name|text
operator|.
name|NumberFormat
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|AbstractMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|SortedMap
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|thrift
operator|.
name|generated
operator|.
name|AlreadyExists
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|thrift
operator|.
name|generated
operator|.
name|ColumnDescriptor
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|thrift
operator|.
name|generated
operator|.
name|Hbase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|thrift
operator|.
name|generated
operator|.
name|IOError
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|thrift
operator|.
name|generated
operator|.
name|IllegalArgument
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|thrift
operator|.
name|generated
operator|.
name|Mutation
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|thrift
operator|.
name|generated
operator|.
name|NotFound
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|thrift
operator|.
name|generated
operator|.
name|ScanEntry
import|;
end_import

begin_import
import|import
name|com
operator|.
name|facebook
operator|.
name|thrift
operator|.
name|TException
import|;
end_import

begin_import
import|import
name|com
operator|.
name|facebook
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TBinaryProtocol
import|;
end_import

begin_import
import|import
name|com
operator|.
name|facebook
operator|.
name|thrift
operator|.
name|protocol
operator|.
name|TProtocol
import|;
end_import

begin_import
import|import
name|com
operator|.
name|facebook
operator|.
name|thrift
operator|.
name|transport
operator|.
name|TSocket
import|;
end_import

begin_import
import|import
name|com
operator|.
name|facebook
operator|.
name|thrift
operator|.
name|transport
operator|.
name|TTransport
import|;
end_import

begin_class
specifier|public
class|class
name|DemoClient
block|{
specifier|protected
name|int
name|port
init|=
literal|9090
decl_stmt|;
name|CharsetDecoder
name|decoder
init|=
literal|null
decl_stmt|;
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|IOError
throws|,
name|TException
throws|,
name|NotFound
throws|,
name|UnsupportedEncodingException
throws|,
name|IllegalArgument
throws|,
name|AlreadyExists
block|{
name|DemoClient
name|client
init|=
operator|new
name|DemoClient
argument_list|()
decl_stmt|;
name|client
operator|.
name|run
argument_list|()
expr_stmt|;
block|}
name|DemoClient
parameter_list|()
block|{
name|decoder
operator|=
name|Charset
operator|.
name|forName
argument_list|(
literal|"UTF-8"
argument_list|)
operator|.
name|newDecoder
argument_list|()
expr_stmt|;
block|}
comment|// Helper to translate byte[]'s to UTF8 strings
specifier|private
name|String
name|utf8
parameter_list|(
name|byte
index|[]
name|buf
parameter_list|)
block|{
try|try
block|{
return|return
name|decoder
operator|.
name|decode
argument_list|(
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|buf
argument_list|)
argument_list|)
operator|.
name|toString
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|CharacterCodingException
name|e
parameter_list|)
block|{
return|return
literal|"[INVALID UTF-8]"
return|;
block|}
block|}
comment|// Helper to translate strings to UTF8 bytes
specifier|private
name|byte
index|[]
name|bytes
parameter_list|(
name|String
name|s
parameter_list|)
block|{
try|try
block|{
return|return
name|s
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|UnsupportedEncodingException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
specifier|private
name|void
name|run
parameter_list|()
throws|throws
name|IOError
throws|,
name|TException
throws|,
name|NotFound
throws|,
name|IllegalArgument
throws|,
name|AlreadyExists
block|{
name|TTransport
name|transport
init|=
operator|new
name|TSocket
argument_list|(
literal|"localhost"
argument_list|,
name|port
argument_list|)
decl_stmt|;
name|TProtocol
name|protocol
init|=
operator|new
name|TBinaryProtocol
argument_list|(
name|transport
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|Hbase
operator|.
name|Client
name|client
init|=
operator|new
name|Hbase
operator|.
name|Client
argument_list|(
name|protocol
argument_list|)
decl_stmt|;
name|transport
operator|.
name|open
argument_list|()
expr_stmt|;
name|byte
index|[]
name|t
init|=
name|bytes
argument_list|(
literal|"demo_table"
argument_list|)
decl_stmt|;
comment|//
comment|// Scan all tables, look for the demo table and delete it.
comment|//
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"scanning tables..."
argument_list|)
expr_stmt|;
for|for
control|(
name|byte
index|[]
name|name
range|:
name|client
operator|.
name|getTableNames
argument_list|()
control|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"  found: "
operator|+
name|utf8
argument_list|(
name|name
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|utf8
argument_list|(
name|name
argument_list|)
operator|.
name|equals
argument_list|(
name|utf8
argument_list|(
name|t
argument_list|)
argument_list|)
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"    deleting table: "
operator|+
name|utf8
argument_list|(
name|name
argument_list|)
argument_list|)
expr_stmt|;
name|client
operator|.
name|deleteTable
argument_list|(
name|name
argument_list|)
expr_stmt|;
block|}
block|}
comment|//
comment|// Create the demo table with two column families, entry: and unused:
comment|//
name|ArrayList
argument_list|<
name|ColumnDescriptor
argument_list|>
name|columns
init|=
operator|new
name|ArrayList
argument_list|<
name|ColumnDescriptor
argument_list|>
argument_list|()
decl_stmt|;
name|ColumnDescriptor
name|col
init|=
literal|null
decl_stmt|;
name|col
operator|=
operator|new
name|ColumnDescriptor
argument_list|()
expr_stmt|;
name|col
operator|.
name|name
operator|=
name|bytes
argument_list|(
literal|"entry:"
argument_list|)
expr_stmt|;
name|col
operator|.
name|maxVersions
operator|=
literal|10
expr_stmt|;
name|columns
operator|.
name|add
argument_list|(
name|col
argument_list|)
expr_stmt|;
name|col
operator|=
operator|new
name|ColumnDescriptor
argument_list|()
expr_stmt|;
name|col
operator|.
name|name
operator|=
name|bytes
argument_list|(
literal|"unused:"
argument_list|)
expr_stmt|;
name|columns
operator|.
name|add
argument_list|(
name|col
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"creating table: "
operator|+
name|utf8
argument_list|(
name|t
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
name|client
operator|.
name|createTable
argument_list|(
name|t
argument_list|,
name|columns
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|AlreadyExists
name|ae
parameter_list|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"WARN: "
operator|+
name|ae
operator|.
name|message
argument_list|)
expr_stmt|;
block|}
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"column families in "
operator|+
name|utf8
argument_list|(
name|t
argument_list|)
operator|+
literal|": "
argument_list|)
expr_stmt|;
name|AbstractMap
argument_list|<
name|byte
index|[]
argument_list|,
name|ColumnDescriptor
argument_list|>
name|columnMap
init|=
name|client
operator|.
name|getColumnDescriptors
argument_list|(
name|t
argument_list|)
decl_stmt|;
for|for
control|(
name|ColumnDescriptor
name|col2
range|:
name|columnMap
operator|.
name|values
argument_list|()
control|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"  column: "
operator|+
name|utf8
argument_list|(
name|col2
operator|.
name|name
argument_list|)
operator|+
literal|", maxVer: "
operator|+
name|Integer
operator|.
name|toString
argument_list|(
name|col2
operator|.
name|maxVersions
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|//
comment|// Test UTF-8 handling
comment|//
name|byte
index|[]
name|invalid
init|=
block|{
operator|(
name|byte
operator|)
literal|'f'
block|,
operator|(
name|byte
operator|)
literal|'o'
block|,
operator|(
name|byte
operator|)
literal|'o'
block|,
operator|(
name|byte
operator|)
literal|'-'
block|,
operator|(
name|byte
operator|)
literal|0xfc
block|,
operator|(
name|byte
operator|)
literal|0xa1
block|,
operator|(
name|byte
operator|)
literal|0xa1
block|,
operator|(
name|byte
operator|)
literal|0xa1
block|,
operator|(
name|byte
operator|)
literal|0xa1
block|}
decl_stmt|;
name|byte
index|[]
name|valid
init|=
block|{
operator|(
name|byte
operator|)
literal|'f'
block|,
operator|(
name|byte
operator|)
literal|'o'
block|,
operator|(
name|byte
operator|)
literal|'o'
block|,
operator|(
name|byte
operator|)
literal|'-'
block|,
operator|(
name|byte
operator|)
literal|0xE7
block|,
operator|(
name|byte
operator|)
literal|0x94
block|,
operator|(
name|byte
operator|)
literal|0x9F
block|,
operator|(
name|byte
operator|)
literal|0xE3
block|,
operator|(
name|byte
operator|)
literal|0x83
block|,
operator|(
name|byte
operator|)
literal|0x93
block|,
operator|(
name|byte
operator|)
literal|0xE3
block|,
operator|(
name|byte
operator|)
literal|0x83
block|,
operator|(
name|byte
operator|)
literal|0xBC
block|,
operator|(
name|byte
operator|)
literal|0xE3
block|,
operator|(
name|byte
operator|)
literal|0x83
block|,
operator|(
name|byte
operator|)
literal|0xAB
block|}
decl_stmt|;
comment|// non-utf8 is fine for data
name|client
operator|.
name|put
argument_list|(
name|t
argument_list|,
name|bytes
argument_list|(
literal|"foo"
argument_list|)
argument_list|,
name|bytes
argument_list|(
literal|"entry:foo"
argument_list|)
argument_list|,
name|invalid
argument_list|)
expr_stmt|;
comment|// try empty strings
name|client
operator|.
name|put
argument_list|(
name|t
argument_list|,
name|bytes
argument_list|(
literal|""
argument_list|)
argument_list|,
name|bytes
argument_list|(
literal|"entry:"
argument_list|)
argument_list|,
name|bytes
argument_list|(
literal|""
argument_list|)
argument_list|)
expr_stmt|;
comment|// this row name is valid utf8
name|client
operator|.
name|put
argument_list|(
name|t
argument_list|,
name|valid
argument_list|,
name|bytes
argument_list|(
literal|"entry:foo"
argument_list|)
argument_list|,
name|valid
argument_list|)
expr_stmt|;
comment|// non-utf8 is not allowed in row names
try|try
block|{
name|client
operator|.
name|put
argument_list|(
name|t
argument_list|,
name|invalid
argument_list|,
name|bytes
argument_list|(
literal|"entry:foo"
argument_list|)
argument_list|,
name|invalid
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"FATAL: shouldn't get here"
argument_list|)
expr_stmt|;
name|System
operator|.
name|exit
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOError
name|e
parameter_list|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"expected error: "
operator|+
name|e
operator|.
name|message
argument_list|)
expr_stmt|;
block|}
comment|// Run a scanner on the rows we just created
name|ArrayList
argument_list|<
name|byte
index|[]
argument_list|>
name|columnNames
init|=
operator|new
name|ArrayList
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|()
decl_stmt|;
name|columnNames
operator|.
name|add
argument_list|(
name|bytes
argument_list|(
literal|"entry:"
argument_list|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Starting scanner..."
argument_list|)
expr_stmt|;
name|int
name|scanner
init|=
name|client
operator|.
name|scannerOpen
argument_list|(
name|t
argument_list|,
name|bytes
argument_list|(
literal|""
argument_list|)
argument_list|,
name|columnNames
argument_list|)
decl_stmt|;
try|try
block|{
while|while
condition|(
literal|true
condition|)
block|{
name|ScanEntry
name|value
init|=
name|client
operator|.
name|scannerGet
argument_list|(
name|scanner
argument_list|)
decl_stmt|;
name|printEntry
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|NotFound
name|nf
parameter_list|)
block|{
name|client
operator|.
name|scannerClose
argument_list|(
name|scanner
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Scanner finished"
argument_list|)
expr_stmt|;
block|}
comment|//
comment|// Run some operations on a bunch of rows
comment|//
for|for
control|(
name|int
name|i
init|=
literal|100
init|;
name|i
operator|>=
literal|0
condition|;
operator|--
name|i
control|)
block|{
comment|// format row keys as "00000" to "00100"
name|NumberFormat
name|nf
init|=
name|NumberFormat
operator|.
name|getInstance
argument_list|()
decl_stmt|;
name|nf
operator|.
name|setMinimumIntegerDigits
argument_list|(
literal|5
argument_list|)
expr_stmt|;
name|nf
operator|.
name|setGroupingUsed
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|byte
index|[]
name|row
init|=
name|bytes
argument_list|(
name|nf
operator|.
name|format
argument_list|(
name|i
argument_list|)
argument_list|)
decl_stmt|;
name|client
operator|.
name|put
argument_list|(
name|t
argument_list|,
name|row
argument_list|,
name|bytes
argument_list|(
literal|"unused:"
argument_list|)
argument_list|,
name|bytes
argument_list|(
literal|"DELETE_ME"
argument_list|)
argument_list|)
expr_stmt|;
name|printRow
argument_list|(
name|row
argument_list|,
name|client
operator|.
name|getRow
argument_list|(
name|t
argument_list|,
name|row
argument_list|)
argument_list|)
expr_stmt|;
name|client
operator|.
name|deleteAllRow
argument_list|(
name|t
argument_list|,
name|row
argument_list|)
expr_stmt|;
name|client
operator|.
name|put
argument_list|(
name|t
argument_list|,
name|row
argument_list|,
name|bytes
argument_list|(
literal|"entry:num"
argument_list|)
argument_list|,
name|bytes
argument_list|(
literal|"0"
argument_list|)
argument_list|)
expr_stmt|;
name|client
operator|.
name|put
argument_list|(
name|t
argument_list|,
name|row
argument_list|,
name|bytes
argument_list|(
literal|"entry:foo"
argument_list|)
argument_list|,
name|bytes
argument_list|(
literal|"FOO"
argument_list|)
argument_list|)
expr_stmt|;
name|printRow
argument_list|(
name|row
argument_list|,
name|client
operator|.
name|getRow
argument_list|(
name|t
argument_list|,
name|row
argument_list|)
argument_list|)
expr_stmt|;
name|Mutation
name|m
init|=
literal|null
decl_stmt|;
name|ArrayList
argument_list|<
name|Mutation
argument_list|>
name|mutations
init|=
operator|new
name|ArrayList
argument_list|<
name|Mutation
argument_list|>
argument_list|()
decl_stmt|;
name|m
operator|=
operator|new
name|Mutation
argument_list|()
expr_stmt|;
name|m
operator|.
name|column
operator|=
name|bytes
argument_list|(
literal|"entry:foo"
argument_list|)
expr_stmt|;
name|m
operator|.
name|isDelete
operator|=
literal|true
expr_stmt|;
name|mutations
operator|.
name|add
argument_list|(
name|m
argument_list|)
expr_stmt|;
name|m
operator|=
operator|new
name|Mutation
argument_list|()
expr_stmt|;
name|m
operator|.
name|column
operator|=
name|bytes
argument_list|(
literal|"entry:num"
argument_list|)
expr_stmt|;
name|m
operator|.
name|value
operator|=
name|bytes
argument_list|(
literal|"-1"
argument_list|)
expr_stmt|;
name|mutations
operator|.
name|add
argument_list|(
name|m
argument_list|)
expr_stmt|;
name|client
operator|.
name|mutateRow
argument_list|(
name|t
argument_list|,
name|row
argument_list|,
name|mutations
argument_list|)
expr_stmt|;
name|printRow
argument_list|(
name|row
argument_list|,
name|client
operator|.
name|getRow
argument_list|(
name|t
argument_list|,
name|row
argument_list|)
argument_list|)
expr_stmt|;
name|client
operator|.
name|put
argument_list|(
name|t
argument_list|,
name|row
argument_list|,
name|bytes
argument_list|(
literal|"entry:num"
argument_list|)
argument_list|,
name|bytes
argument_list|(
name|Integer
operator|.
name|toString
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|client
operator|.
name|put
argument_list|(
name|t
argument_list|,
name|row
argument_list|,
name|bytes
argument_list|(
literal|"entry:sqr"
argument_list|)
argument_list|,
name|bytes
argument_list|(
name|Integer
operator|.
name|toString
argument_list|(
name|i
operator|*
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|printRow
argument_list|(
name|row
argument_list|,
name|client
operator|.
name|getRow
argument_list|(
name|t
argument_list|,
name|row
argument_list|)
argument_list|)
expr_stmt|;
comment|// sleep to force later timestamp
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|50
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
comment|// no-op
block|}
name|mutations
operator|.
name|clear
argument_list|()
expr_stmt|;
name|m
operator|=
operator|new
name|Mutation
argument_list|()
expr_stmt|;
name|m
operator|.
name|column
operator|=
name|bytes
argument_list|(
literal|"entry:num"
argument_list|)
expr_stmt|;
name|m
operator|.
name|value
operator|=
name|bytes
argument_list|(
literal|"-999"
argument_list|)
expr_stmt|;
name|mutations
operator|.
name|add
argument_list|(
name|m
argument_list|)
expr_stmt|;
name|m
operator|=
operator|new
name|Mutation
argument_list|()
expr_stmt|;
name|m
operator|.
name|column
operator|=
name|bytes
argument_list|(
literal|"entry:sqr"
argument_list|)
expr_stmt|;
name|m
operator|.
name|isDelete
operator|=
literal|true
expr_stmt|;
name|client
operator|.
name|mutateRowTs
argument_list|(
name|t
argument_list|,
name|row
argument_list|,
name|mutations
argument_list|,
literal|1
argument_list|)
expr_stmt|;
comment|// shouldn't override latest
name|printRow
argument_list|(
name|row
argument_list|,
name|client
operator|.
name|getRow
argument_list|(
name|t
argument_list|,
name|row
argument_list|)
argument_list|)
expr_stmt|;
name|ArrayList
argument_list|<
name|byte
index|[]
argument_list|>
name|versions
init|=
name|client
operator|.
name|getVer
argument_list|(
name|t
argument_list|,
name|row
argument_list|,
name|bytes
argument_list|(
literal|"entry:num"
argument_list|)
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|printVersions
argument_list|(
name|row
argument_list|,
name|versions
argument_list|)
expr_stmt|;
if|if
condition|(
name|versions
operator|.
name|size
argument_list|()
operator|!=
literal|4
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"FATAL: wrong # of versions"
argument_list|)
expr_stmt|;
name|System
operator|.
name|exit
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|client
operator|.
name|get
argument_list|(
name|t
argument_list|,
name|row
argument_list|,
name|bytes
argument_list|(
literal|"entry:foo"
argument_list|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"FATAL: shouldn't get here"
argument_list|)
expr_stmt|;
name|System
operator|.
name|exit
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NotFound
name|nf2
parameter_list|)
block|{
comment|// blank
block|}
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|""
argument_list|)
expr_stmt|;
block|}
comment|// scan all rows/columnNames
name|columnNames
operator|.
name|clear
argument_list|()
expr_stmt|;
for|for
control|(
name|ColumnDescriptor
name|col2
range|:
name|client
operator|.
name|getColumnDescriptors
argument_list|(
name|t
argument_list|)
operator|.
name|values
argument_list|()
control|)
block|{
name|columnNames
operator|.
name|add
argument_list|(
name|col2
operator|.
name|name
argument_list|)
expr_stmt|;
block|}
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Starting scanner..."
argument_list|)
expr_stmt|;
name|scanner
operator|=
name|client
operator|.
name|scannerOpenWithStop
argument_list|(
name|t
argument_list|,
name|bytes
argument_list|(
literal|"00020"
argument_list|)
argument_list|,
name|bytes
argument_list|(
literal|"00040"
argument_list|)
argument_list|,
name|columnNames
argument_list|)
expr_stmt|;
try|try
block|{
while|while
condition|(
literal|true
condition|)
block|{
name|ScanEntry
name|value
init|=
name|client
operator|.
name|scannerGet
argument_list|(
name|scanner
argument_list|)
decl_stmt|;
name|printEntry
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|NotFound
name|nf
parameter_list|)
block|{
name|client
operator|.
name|scannerClose
argument_list|(
name|scanner
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Scanner finished"
argument_list|)
expr_stmt|;
block|}
name|transport
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
specifier|private
specifier|final
name|void
name|printVersions
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|ArrayList
argument_list|<
name|byte
index|[]
argument_list|>
name|values
parameter_list|)
block|{
name|StringBuilder
name|rowStr
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|byte
index|[]
name|value
range|:
name|values
control|)
block|{
name|rowStr
operator|.
name|append
argument_list|(
name|utf8
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
name|rowStr
operator|.
name|append
argument_list|(
literal|"; "
argument_list|)
expr_stmt|;
block|}
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"row: "
operator|+
name|utf8
argument_list|(
name|row
argument_list|)
operator|+
literal|", values: "
operator|+
name|rowStr
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|final
name|void
name|printEntry
parameter_list|(
name|ScanEntry
name|entry
parameter_list|)
block|{
name|printRow
argument_list|(
name|entry
operator|.
name|row
argument_list|,
name|entry
operator|.
name|columns
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|final
name|void
name|printRow
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|AbstractMap
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
name|values
parameter_list|)
block|{
comment|// copy values into a TreeMap to get them in sorted order
name|TreeMap
argument_list|<
name|String
argument_list|,
name|byte
index|[]
argument_list|>
name|sorted
init|=
operator|new
name|TreeMap
argument_list|<
name|String
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|AbstractMap
operator|.
name|Entry
argument_list|<
name|byte
index|[]
argument_list|,
name|byte
index|[]
argument_list|>
name|entry
range|:
name|values
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|sorted
operator|.
name|put
argument_list|(
name|utf8
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|StringBuilder
name|rowStr
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|SortedMap
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|byte
index|[]
argument_list|>
name|entry
range|:
name|sorted
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|rowStr
operator|.
name|append
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
name|rowStr
operator|.
name|append
argument_list|(
literal|" => "
argument_list|)
expr_stmt|;
name|rowStr
operator|.
name|append
argument_list|(
name|utf8
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|rowStr
operator|.
name|append
argument_list|(
literal|"; "
argument_list|)
expr_stmt|;
block|}
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"row: "
operator|+
name|utf8
argument_list|(
name|row
argument_list|)
operator|+
literal|", cols: "
operator|+
name|rowStr
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

