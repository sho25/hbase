begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|security
operator|.
name|provider
package|;
end_package

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
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|InvocationTargetException
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
name|Optional
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ServiceLoader
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicReference
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Collectors
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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|final
class|class
name|SaslServerAuthenticationProviders
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|SaslClientAuthenticationProviders
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|EXTRA_PROVIDERS_KEY
init|=
literal|"hbase.server.sasl.provider.extras"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|AtomicReference
argument_list|<
name|SaslServerAuthenticationProviders
argument_list|>
name|holder
init|=
operator|new
name|AtomicReference
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|HashMap
argument_list|<
name|Byte
argument_list|,
name|SaslServerAuthenticationProvider
argument_list|>
name|providers
decl_stmt|;
specifier|private
name|SaslServerAuthenticationProviders
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|HashMap
argument_list|<
name|Byte
argument_list|,
name|SaslServerAuthenticationProvider
argument_list|>
name|providers
parameter_list|)
block|{
name|this
operator|.
name|providers
operator|=
name|providers
expr_stmt|;
block|}
comment|/**    * Returns the number of registered providers.    */
specifier|public
name|int
name|getNumRegisteredProviders
parameter_list|()
block|{
return|return
name|providers
operator|.
name|size
argument_list|()
return|;
block|}
comment|/**    * Returns a singleton instance of {@link SaslServerAuthenticationProviders}.    */
specifier|public
specifier|static
name|SaslServerAuthenticationProviders
name|getInstance
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|SaslServerAuthenticationProviders
name|providers
init|=
name|holder
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
literal|null
operator|==
name|providers
condition|)
block|{
synchronized|synchronized
init|(
name|holder
init|)
block|{
comment|// Someone else beat us here
name|providers
operator|=
name|holder
operator|.
name|get
argument_list|()
expr_stmt|;
if|if
condition|(
literal|null
operator|!=
name|providers
condition|)
block|{
return|return
name|providers
return|;
block|}
name|providers
operator|=
name|createProviders
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|holder
operator|.
name|set
argument_list|(
name|providers
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|providers
return|;
block|}
comment|/**    * Removes the cached singleton instance of {@link SaslServerAuthenticationProviders}.    */
specifier|public
specifier|static
name|void
name|reset
parameter_list|()
block|{
synchronized|synchronized
init|(
name|holder
init|)
block|{
name|holder
operator|.
name|set
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Adds the given provider into the map of providers if a mapping for the auth code does not    * already exist in the map.    */
specifier|static
name|void
name|addProviderIfNotExists
parameter_list|(
name|SaslServerAuthenticationProvider
name|provider
parameter_list|,
name|HashMap
argument_list|<
name|Byte
argument_list|,
name|SaslServerAuthenticationProvider
argument_list|>
name|providers
parameter_list|)
block|{
specifier|final
name|byte
name|newProviderAuthCode
init|=
name|provider
operator|.
name|getSaslAuthMethod
argument_list|()
operator|.
name|getCode
argument_list|()
decl_stmt|;
specifier|final
name|SaslServerAuthenticationProvider
name|alreadyRegisteredProvider
init|=
name|providers
operator|.
name|get
argument_list|(
name|newProviderAuthCode
argument_list|)
decl_stmt|;
if|if
condition|(
name|alreadyRegisteredProvider
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Trying to load SaslServerAuthenticationProvider "
operator|+
name|provider
operator|.
name|getClass
argument_list|()
operator|+
literal|", but "
operator|+
name|alreadyRegisteredProvider
operator|.
name|getClass
argument_list|()
operator|+
literal|" is already registered with the same auth code"
argument_list|)
throw|;
block|}
name|providers
operator|.
name|put
argument_list|(
name|newProviderAuthCode
argument_list|,
name|provider
argument_list|)
expr_stmt|;
block|}
comment|/**    * Adds any providers defined in the configuration.    */
specifier|static
name|void
name|addExtraProviders
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|HashMap
argument_list|<
name|Byte
argument_list|,
name|SaslServerAuthenticationProvider
argument_list|>
name|providers
parameter_list|)
block|{
for|for
control|(
name|String
name|implName
range|:
name|conf
operator|.
name|getStringCollection
argument_list|(
name|EXTRA_PROVIDERS_KEY
argument_list|)
control|)
block|{
name|Class
argument_list|<
name|?
argument_list|>
name|clz
decl_stmt|;
try|try
block|{
name|clz
operator|=
name|Class
operator|.
name|forName
argument_list|(
name|implName
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed to find SaslServerAuthenticationProvider class {}"
argument_list|,
name|implName
argument_list|,
name|e
argument_list|)
expr_stmt|;
continue|continue;
block|}
if|if
condition|(
operator|!
name|SaslServerAuthenticationProvider
operator|.
name|class
operator|.
name|isAssignableFrom
argument_list|(
name|clz
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Server authentication class {} is not an instance of "
operator|+
literal|"SaslServerAuthenticationProvider"
argument_list|,
name|clz
argument_list|)
expr_stmt|;
continue|continue;
block|}
try|try
block|{
name|SaslServerAuthenticationProvider
name|provider
init|=
operator|(
name|SaslServerAuthenticationProvider
operator|)
name|clz
operator|.
name|getConstructor
argument_list|()
operator|.
name|newInstance
argument_list|()
decl_stmt|;
name|addProviderIfNotExists
argument_list|(
name|provider
argument_list|,
name|providers
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InstantiationException
decl||
name|IllegalAccessException
decl||
name|NoSuchMethodException
decl||
name|InvocationTargetException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed to instantiate {}"
argument_list|,
name|clz
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Loads server authentication providers from the classpath and configuration, and then creates    * the SaslServerAuthenticationProviders instance.    */
specifier|static
name|SaslServerAuthenticationProviders
name|createProviders
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|ServiceLoader
argument_list|<
name|SaslServerAuthenticationProvider
argument_list|>
name|loader
init|=
name|ServiceLoader
operator|.
name|load
argument_list|(
name|SaslServerAuthenticationProvider
operator|.
name|class
argument_list|)
decl_stmt|;
name|HashMap
argument_list|<
name|Byte
argument_list|,
name|SaslServerAuthenticationProvider
argument_list|>
name|providers
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|SaslServerAuthenticationProvider
name|provider
range|:
name|loader
control|)
block|{
name|addProviderIfNotExists
argument_list|(
name|provider
argument_list|,
name|providers
argument_list|)
expr_stmt|;
block|}
name|addExtraProviders
argument_list|(
name|conf
argument_list|,
name|providers
argument_list|)
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|String
name|loadedProviders
init|=
name|providers
operator|.
name|values
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
parameter_list|(
name|provider
parameter_list|)
lambda|->
name|provider
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|joining
argument_list|(
literal|", "
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|loadedProviders
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|loadedProviders
operator|=
literal|"None!"
expr_stmt|;
block|}
name|LOG
operator|.
name|trace
argument_list|(
literal|"Found SaslServerAuthenticationProviders {}"
argument_list|,
name|loadedProviders
argument_list|)
expr_stmt|;
block|}
comment|// Initialize the providers once, before we get into the RPC path.
name|providers
operator|.
name|forEach
argument_list|(
parameter_list|(
name|b
parameter_list|,
name|provider
parameter_list|)
lambda|->
block|{
try|try
block|{
comment|// Give them a copy, just to make sure there is no funny-business going on.
name|provider
operator|.
name|init
argument_list|(
operator|new
name|Configuration
argument_list|(
name|conf
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed to initialize {}"
argument_list|,
name|provider
operator|.
name|getClass
argument_list|()
argument_list|,
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Failed to initialize "
operator|+
name|provider
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
argument_list|)
expr_stmt|;
return|return
operator|new
name|SaslServerAuthenticationProviders
argument_list|(
name|conf
argument_list|,
name|providers
argument_list|)
return|;
block|}
comment|/**    * Selects the appropriate SaslServerAuthenticationProvider from those available. If there is no    * matching provider for the given {@code authByte}, this method will return null.    */
specifier|public
name|SaslServerAuthenticationProvider
name|selectProvider
parameter_list|(
name|byte
name|authByte
parameter_list|)
block|{
return|return
name|providers
operator|.
name|get
argument_list|(
name|Byte
operator|.
name|valueOf
argument_list|(
name|authByte
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Extracts the SIMPLE authentication provider.    */
specifier|public
name|SaslServerAuthenticationProvider
name|getSimpleProvider
parameter_list|()
block|{
name|Optional
argument_list|<
name|SaslServerAuthenticationProvider
argument_list|>
name|opt
init|=
name|providers
operator|.
name|values
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|filter
argument_list|(
parameter_list|(
name|p
parameter_list|)
lambda|->
name|p
operator|instanceof
name|SimpleSaslServerAuthenticationProvider
argument_list|)
operator|.
name|findFirst
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|opt
operator|.
name|isPresent
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"SIMPLE authentication provider not available when it should be"
argument_list|)
throw|;
block|}
return|return
name|opt
operator|.
name|get
argument_list|()
return|;
block|}
block|}
end_class

end_unit
