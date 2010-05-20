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
name|filter
package|;
end_package

begin_comment
comment|/**  * Used to indicate a filter incompatibility  */
end_comment

begin_class
specifier|public
class|class
name|IncompatibleFilterException
extends|extends
name|RuntimeException
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|3236763276623198231L
decl_stmt|;
comment|/** constructor */
specifier|public
name|IncompatibleFilterException
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
comment|/**    * constructor    * @param s message    */
specifier|public
name|IncompatibleFilterException
parameter_list|(
name|String
name|s
parameter_list|)
block|{
name|super
argument_list|(
name|s
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

