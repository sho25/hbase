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
name|constraint
package|;
end_package

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
name|client
operator|.
name|Put
import|;
end_import

begin_comment
comment|/**  * Always non-gracefully fail on attempt  */
end_comment

begin_class
specifier|public
class|class
name|RuntimeFailConstraint
extends|extends
name|BaseConstraint
block|{
annotation|@
name|Override
specifier|public
name|void
name|check
parameter_list|(
name|Put
name|p
parameter_list|)
throws|throws
name|ConstraintException
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"RuntimeFailConstraint always throws a runtime exception"
argument_list|)
throw|;
block|}
block|}
end_class

end_unit

