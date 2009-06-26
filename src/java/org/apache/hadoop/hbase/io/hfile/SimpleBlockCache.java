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
name|io
operator|.
name|hfile
package|;
end_package

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|ref
operator|.
name|ReferenceQueue
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|ref
operator|.
name|SoftReference
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
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_comment
comment|/**  * Simple one RFile soft reference cache.  */
end_comment

begin_class
specifier|public
class|class
name|SimpleBlockCache
implements|implements
name|BlockCache
block|{
specifier|private
specifier|static
class|class
name|Ref
extends|extends
name|SoftReference
argument_list|<
name|ByteBuffer
argument_list|>
block|{
specifier|public
name|String
name|blockId
decl_stmt|;
specifier|public
name|Ref
parameter_list|(
name|String
name|blockId
parameter_list|,
name|ByteBuffer
name|buf
parameter_list|,
name|ReferenceQueue
name|q
parameter_list|)
block|{
name|super
argument_list|(
name|buf
argument_list|,
name|q
argument_list|)
expr_stmt|;
name|this
operator|.
name|blockId
operator|=
name|blockId
expr_stmt|;
block|}
block|}
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Ref
argument_list|>
name|cache
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Ref
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
name|ReferenceQueue
name|q
init|=
operator|new
name|ReferenceQueue
argument_list|()
decl_stmt|;
specifier|public
name|int
name|dumps
init|=
literal|0
decl_stmt|;
comment|/**    * Constructor    */
specifier|public
name|SimpleBlockCache
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
name|void
name|processQueue
parameter_list|()
block|{
name|Ref
name|r
decl_stmt|;
while|while
condition|(
operator|(
name|r
operator|=
operator|(
name|Ref
operator|)
name|q
operator|.
name|poll
argument_list|()
operator|)
operator|!=
literal|null
condition|)
block|{
name|cache
operator|.
name|remove
argument_list|(
name|r
operator|.
name|blockId
argument_list|)
expr_stmt|;
name|dumps
operator|++
expr_stmt|;
block|}
block|}
comment|/**    * @return the size    */
specifier|public
specifier|synchronized
name|int
name|size
parameter_list|()
block|{
name|processQueue
argument_list|()
expr_stmt|;
return|return
name|cache
operator|.
name|size
argument_list|()
return|;
block|}
specifier|public
specifier|synchronized
name|ByteBuffer
name|getBlock
parameter_list|(
name|String
name|blockName
parameter_list|)
block|{
name|processQueue
argument_list|()
expr_stmt|;
comment|// clear out some crap.
name|Ref
name|ref
init|=
name|cache
operator|.
name|get
argument_list|(
name|blockName
argument_list|)
decl_stmt|;
if|if
condition|(
name|ref
operator|==
literal|null
condition|)
return|return
literal|null
return|;
return|return
name|ref
operator|.
name|get
argument_list|()
return|;
block|}
specifier|public
specifier|synchronized
name|void
name|cacheBlock
parameter_list|(
name|String
name|blockName
parameter_list|,
name|ByteBuffer
name|buf
parameter_list|)
block|{
name|cache
operator|.
name|put
argument_list|(
name|blockName
argument_list|,
operator|new
name|Ref
argument_list|(
name|blockName
argument_list|,
name|buf
argument_list|,
name|q
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|synchronized
name|void
name|cacheBlock
parameter_list|(
name|String
name|blockName
parameter_list|,
name|ByteBuffer
name|buf
parameter_list|,
name|boolean
name|inMemory
parameter_list|)
block|{
name|cache
operator|.
name|put
argument_list|(
name|blockName
argument_list|,
operator|new
name|Ref
argument_list|(
name|blockName
argument_list|,
name|buf
argument_list|,
name|q
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

