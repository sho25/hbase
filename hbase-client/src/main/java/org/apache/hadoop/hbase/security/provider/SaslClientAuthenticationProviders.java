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
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
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
name|hadoop
operator|.
name|hbase
operator|.
name|HBaseInterfaceAudience
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
name|security
operator|.
name|User
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
name|Pair
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
name|security
operator|.
name|token
operator|.
name|Token
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
name|security
operator|.
name|token
operator|.
name|TokenIdentifier
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
name|apache
operator|.
name|yetus
operator|.
name|audience
operator|.
name|InterfaceStability
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

begin_comment
comment|/**  * Accessor for all SaslAuthenticationProvider instances.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
name|HBaseInterfaceAudience
operator|.
name|AUTHENTICATION
argument_list|)
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
specifier|final
class|class
name|SaslClientAuthenticationProviders
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
name|SELECTOR_KEY
init|=
literal|"hbase.client.sasl.provider.class"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|EXTRA_PROVIDERS_KEY
init|=
literal|"hbase.client.sasl.provider.extras"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|AtomicReference
argument_list|<
name|SaslClientAuthenticationProviders
argument_list|>
name|providersRef
init|=
operator|new
name|AtomicReference
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|Collection
argument_list|<
name|SaslClientAuthenticationProvider
argument_list|>
name|providers
decl_stmt|;
specifier|private
specifier|final
name|AuthenticationProviderSelector
name|selector
decl_stmt|;
specifier|private
name|SaslClientAuthenticationProviders
parameter_list|(
name|Collection
argument_list|<
name|SaslClientAuthenticationProvider
argument_list|>
name|providers
parameter_list|,
name|AuthenticationProviderSelector
name|selector
parameter_list|)
block|{
name|this
operator|.
name|providers
operator|=
name|providers
expr_stmt|;
name|this
operator|.
name|selector
operator|=
name|selector
expr_stmt|;
block|}
comment|/**    * Returns the number of providers that have been registered.    */
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
comment|/**    * Returns a singleton instance of {@link SaslClientAuthenticationProviders}.    */
specifier|public
specifier|static
specifier|synchronized
name|SaslClientAuthenticationProviders
name|getInstance
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|SaslClientAuthenticationProviders
name|providers
init|=
name|providersRef
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|providers
operator|==
literal|null
condition|)
block|{
name|providers
operator|=
name|instantiate
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|providersRef
operator|.
name|set
argument_list|(
name|providers
argument_list|)
expr_stmt|;
block|}
return|return
name|providers
return|;
block|}
comment|/**    * Removes the cached singleton instance of {@link SaslClientAuthenticationProviders}.    */
specifier|public
specifier|static
specifier|synchronized
name|void
name|reset
parameter_list|()
block|{
name|providersRef
operator|.
name|set
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**    * Adds the given {@code provider} to the set, only if an equivalent provider does not    * already exist in the set.    */
specifier|static
name|void
name|addProviderIfNotExists
parameter_list|(
name|SaslClientAuthenticationProvider
name|provider
parameter_list|,
name|HashMap
argument_list|<
name|Byte
argument_list|,
name|SaslClientAuthenticationProvider
argument_list|>
name|providers
parameter_list|)
block|{
name|Byte
name|code
init|=
name|provider
operator|.
name|getSaslAuthMethod
argument_list|()
operator|.
name|getCode
argument_list|()
decl_stmt|;
name|SaslClientAuthenticationProvider
name|existingProvider
init|=
name|providers
operator|.
name|get
argument_list|(
name|code
argument_list|)
decl_stmt|;
if|if
condition|(
name|existingProvider
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Already registered authentication provider with "
operator|+
name|code
operator|+
literal|" "
operator|+
name|existingProvider
operator|.
name|getClass
argument_list|()
argument_list|)
throw|;
block|}
name|providers
operator|.
name|put
argument_list|(
name|code
argument_list|,
name|provider
argument_list|)
expr_stmt|;
block|}
comment|/**    * Instantiates the ProviderSelector implementation from the provided configuration.    */
specifier|static
name|AuthenticationProviderSelector
name|instantiateSelector
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|Collection
argument_list|<
name|SaslClientAuthenticationProvider
argument_list|>
name|providers
parameter_list|)
block|{
name|Class
argument_list|<
name|?
extends|extends
name|AuthenticationProviderSelector
argument_list|>
name|clz
init|=
name|conf
operator|.
name|getClass
argument_list|(
name|SELECTOR_KEY
argument_list|,
name|BuiltInProviderSelector
operator|.
name|class
argument_list|,
name|AuthenticationProviderSelector
operator|.
name|class
argument_list|)
decl_stmt|;
try|try
block|{
name|AuthenticationProviderSelector
name|selector
init|=
name|clz
operator|.
name|getConstructor
argument_list|()
operator|.
name|newInstance
argument_list|()
decl_stmt|;
name|selector
operator|.
name|configure
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
name|LOG
operator|.
name|trace
argument_list|(
literal|"Loaded ProviderSelector {}"
argument_list|,
name|selector
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|selector
return|;
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
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Failed to instantiate "
operator|+
name|clz
operator|+
literal|" as the ProviderSelector defined by "
operator|+
name|SELECTOR_KEY
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**    * Extracts and instantiates authentication providers from the configuration.    */
specifier|static
name|void
name|addExplicitProviders
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|HashMap
argument_list|<
name|Byte
argument_list|,
name|SaslClientAuthenticationProvider
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
comment|// Load the class from the config
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
literal|"Failed to load SaslClientAuthenticationProvider {}"
argument_list|,
name|implName
argument_list|,
name|e
argument_list|)
expr_stmt|;
continue|continue;
block|}
comment|// Make sure it's the right type
if|if
condition|(
operator|!
name|SaslClientAuthenticationProvider
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
literal|"Ignoring SaslClientAuthenticationProvider {} because it is not an instance of"
operator|+
literal|" SaslClientAuthenticationProvider"
argument_list|,
name|clz
argument_list|)
expr_stmt|;
continue|continue;
block|}
comment|// Instantiate it
name|SaslClientAuthenticationProvider
name|provider
decl_stmt|;
try|try
block|{
name|provider
operator|=
operator|(
name|SaslClientAuthenticationProvider
operator|)
name|clz
operator|.
name|getConstructor
argument_list|()
operator|.
name|newInstance
argument_list|()
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
literal|"Failed to instantiate SaslClientAuthenticationProvider {}"
argument_list|,
name|clz
argument_list|,
name|e
argument_list|)
expr_stmt|;
continue|continue;
block|}
comment|// Add it to our set, only if it doesn't conflict with something else we've
comment|// already registered.
name|addProviderIfNotExists
argument_list|(
name|provider
argument_list|,
name|providers
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Instantiates all client authentication providers and returns an instance of    * {@link SaslClientAuthenticationProviders}.    */
specifier|static
name|SaslClientAuthenticationProviders
name|instantiate
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|ServiceLoader
argument_list|<
name|SaslClientAuthenticationProvider
argument_list|>
name|loader
init|=
name|ServiceLoader
operator|.
name|load
argument_list|(
name|SaslClientAuthenticationProvider
operator|.
name|class
argument_list|)
decl_stmt|;
name|HashMap
argument_list|<
name|Byte
argument_list|,
name|SaslClientAuthenticationProvider
argument_list|>
name|providerMap
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|SaslClientAuthenticationProvider
name|provider
range|:
name|loader
control|)
block|{
name|addProviderIfNotExists
argument_list|(
name|provider
argument_list|,
name|providerMap
argument_list|)
expr_stmt|;
block|}
name|addExplicitProviders
argument_list|(
name|conf
argument_list|,
name|providerMap
argument_list|)
expr_stmt|;
name|Collection
argument_list|<
name|SaslClientAuthenticationProvider
argument_list|>
name|providers
init|=
name|Collections
operator|.
name|unmodifiableCollection
argument_list|(
name|providerMap
operator|.
name|values
argument_list|()
argument_list|)
decl_stmt|;
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
name|LOG
operator|.
name|trace
argument_list|(
literal|"Found SaslClientAuthenticationProviders {}"
argument_list|,
name|loadedProviders
argument_list|)
expr_stmt|;
block|}
name|AuthenticationProviderSelector
name|selector
init|=
name|instantiateSelector
argument_list|(
name|conf
argument_list|,
name|providers
argument_list|)
decl_stmt|;
return|return
operator|new
name|SaslClientAuthenticationProviders
argument_list|(
name|providers
argument_list|,
name|selector
argument_list|)
return|;
block|}
comment|/**    * Returns the provider and token pair for SIMPLE authentication.    *    * This method is a "hack" while SIMPLE authentication for HBase does not flow through    * the SASL codepath.    */
specifier|public
name|Pair
argument_list|<
name|SaslClientAuthenticationProvider
argument_list|,
name|Token
argument_list|<
name|?
extends|extends
name|TokenIdentifier
argument_list|>
argument_list|>
name|getSimpleProvider
parameter_list|()
block|{
name|Optional
argument_list|<
name|SaslClientAuthenticationProvider
argument_list|>
name|optional
init|=
name|providers
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
name|SimpleSaslClientAuthenticationProvider
argument_list|)
operator|.
name|findFirst
argument_list|()
decl_stmt|;
return|return
operator|new
name|Pair
argument_list|<>
argument_list|(
name|optional
operator|.
name|get
argument_list|()
argument_list|,
literal|null
argument_list|)
return|;
block|}
comment|/**    * Chooses the best authentication provider and corresponding token given the HBase cluster    * identifier and the user.    */
specifier|public
name|Pair
argument_list|<
name|SaslClientAuthenticationProvider
argument_list|,
name|Token
argument_list|<
name|?
extends|extends
name|TokenIdentifier
argument_list|>
argument_list|>
name|selectProvider
parameter_list|(
name|String
name|clusterId
parameter_list|,
name|User
name|clientUser
parameter_list|)
block|{
return|return
name|selector
operator|.
name|selectProvider
argument_list|(
name|clusterId
argument_list|,
name|clientUser
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|providers
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
parameter_list|(
name|p
parameter_list|)
lambda|->
name|p
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
argument_list|,
literal|"providers=["
argument_list|,
literal|"], selector="
argument_list|)
argument_list|)
operator|+
name|selector
operator|.
name|getClass
argument_list|()
return|;
block|}
block|}
end_class

end_unit

