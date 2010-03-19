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
name|stargate
operator|.
name|auth
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
name|conf
operator|.
name|Configuration
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
name|HBaseConfiguration
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
name|stargate
operator|.
name|User
import|;
end_import

begin_class
specifier|public
class|class
name|HBCAuthenticator
extends|extends
name|Authenticator
block|{
name|Configuration
name|conf
decl_stmt|;
comment|/**    * Default constructor    */
specifier|public
name|HBCAuthenticator
parameter_list|()
block|{
name|this
argument_list|(
name|HBaseConfiguration
operator|.
name|create
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Constructor    * @param conf    */
specifier|public
name|HBCAuthenticator
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|User
name|getUserForToken
parameter_list|(
name|String
name|token
parameter_list|)
block|{
name|String
name|name
init|=
name|conf
operator|.
name|get
argument_list|(
literal|"stargate.auth.token."
operator|+
name|token
argument_list|)
decl_stmt|;
if|if
condition|(
name|name
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|boolean
name|admin
init|=
name|conf
operator|.
name|getBoolean
argument_list|(
literal|"stargate.auth.user."
operator|+
name|name
operator|+
literal|".admin"
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|boolean
name|disabled
init|=
name|conf
operator|.
name|getBoolean
argument_list|(
literal|"stargate.auth.user."
operator|+
name|name
operator|+
literal|".disabled"
argument_list|,
literal|false
argument_list|)
decl_stmt|;
return|return
operator|new
name|User
argument_list|(
name|name
argument_list|,
name|token
argument_list|,
name|admin
argument_list|,
name|disabled
argument_list|)
return|;
block|}
block|}
end_class

end_unit

