begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *   */
end_comment

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
name|io
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
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
name|fs
operator|.
name|FSDataInputStream
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
name|fs
operator|.
name|FSDataOutputStream
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
name|fs
operator|.
name|FileSystem
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
name|fs
operator|.
name|Path
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
name|util
operator|.
name|Bytes
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
name|util
operator|.
name|FSUtils
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
name|io
operator|.
name|Writable
import|;
end_import

begin_comment
comment|/**  * A reference to the top or bottom half of a store file.  The file referenced  * lives under a different region.  References are made at region split time.  *   *<p>References work with a special half store file type.  References know how  * to write out the reference format in the file system and are whats juggled  * when references are mixed in with direct store files.  The half store file  * type is used reading the referred to file.  *  *<p>References to store files located over in some other region look like  * this in the file system  *<code>1278437856009925445.3323223323</code>:  * i.e. an id followed by hash of the referenced region.  * Note, a region is itself not splitable if it has instances of store file  * references.  References are cleaned up by compactions.  */
end_comment

begin_class
specifier|public
class|class
name|Reference
implements|implements
name|Writable
block|{
specifier|private
name|byte
index|[]
name|splitkey
decl_stmt|;
specifier|private
name|Range
name|region
decl_stmt|;
comment|/**     * For split HStoreFiles, it specifies if the file covers the lower half or    * the upper half of the key range    */
specifier|public
specifier|static
enum|enum
name|Range
block|{
comment|/** HStoreFile contains upper half of key range */
name|top
block|,
comment|/** HStoreFile contains lower half of key range */
name|bottom
block|}
comment|/**    * Constructor    * @param r    * @param s This is a serialized storekey with the row we are to split on,    * an empty column and a timestamp of the LATEST_TIMESTAMP.  This is the first    * possible entry in a row.  This is what we are splitting around.    * @param fr    */
specifier|public
name|Reference
parameter_list|(
specifier|final
name|byte
index|[]
name|s
parameter_list|,
specifier|final
name|Range
name|fr
parameter_list|)
block|{
name|this
operator|.
name|splitkey
operator|=
name|s
expr_stmt|;
name|this
operator|.
name|region
operator|=
name|fr
expr_stmt|;
block|}
comment|/**    * Used by serializations.    */
specifier|public
name|Reference
parameter_list|()
block|{
name|this
argument_list|(
literal|null
argument_list|,
name|Range
operator|.
name|bottom
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Range
name|getFileRegion
parameter_list|()
block|{
return|return
name|this
operator|.
name|region
return|;
block|}
specifier|public
name|byte
index|[]
name|getSplitKey
parameter_list|()
block|{
return|return
name|splitkey
return|;
block|}
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|""
operator|+
name|this
operator|.
name|region
return|;
block|}
comment|// Make it serializable.
specifier|public
name|void
name|write
parameter_list|(
name|DataOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Write true if we're doing top of the file.
name|out
operator|.
name|writeBoolean
argument_list|(
name|isTopFileRegion
argument_list|(
name|this
operator|.
name|region
argument_list|)
argument_list|)
expr_stmt|;
name|Bytes
operator|.
name|writeByteArray
argument_list|(
name|out
argument_list|,
name|this
operator|.
name|splitkey
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|readFields
parameter_list|(
name|DataInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|boolean
name|tmp
init|=
name|in
operator|.
name|readBoolean
argument_list|()
decl_stmt|;
comment|// If true, set region to top.
name|this
operator|.
name|region
operator|=
name|tmp
condition|?
name|Range
operator|.
name|top
else|:
name|Range
operator|.
name|bottom
expr_stmt|;
name|this
operator|.
name|splitkey
operator|=
name|Bytes
operator|.
name|readByteArray
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|boolean
name|isTopFileRegion
parameter_list|(
specifier|final
name|Range
name|r
parameter_list|)
block|{
return|return
name|r
operator|.
name|equals
argument_list|(
name|Range
operator|.
name|top
argument_list|)
return|;
block|}
specifier|public
name|Path
name|write
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|p
parameter_list|)
throws|throws
name|IOException
block|{
name|FSUtils
operator|.
name|create
argument_list|(
name|fs
argument_list|,
name|p
argument_list|)
expr_stmt|;
name|FSDataOutputStream
name|out
init|=
name|fs
operator|.
name|create
argument_list|(
name|p
argument_list|)
decl_stmt|;
try|try
block|{
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|out
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
return|return
name|p
return|;
block|}
comment|/**    * Read a Reference from FileSystem.    * @param fs    * @param p    * @return New Reference made from passed<code>p</code>    * @throws IOException    */
specifier|public
specifier|static
name|Reference
name|read
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|p
parameter_list|)
throws|throws
name|IOException
block|{
name|FSDataInputStream
name|in
init|=
name|fs
operator|.
name|open
argument_list|(
name|p
argument_list|)
decl_stmt|;
try|try
block|{
name|Reference
name|r
init|=
operator|new
name|Reference
argument_list|()
decl_stmt|;
name|r
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
return|return
name|r
return|;
block|}
finally|finally
block|{
name|in
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

