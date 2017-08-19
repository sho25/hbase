begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements. See the NOTICE file distributed with this  * work for additional information regarding copyright ownership. The ASF  * licenses this file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the  * License for the specific language governing permissions and limitations under  * the License.  */
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
operator|.
name|crypto
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|BufferedInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|File
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FileInputStream
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
name|java
operator|.
name|io
operator|.
name|InputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URI
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URISyntaxException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URLDecoder
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|Key
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|KeyStore
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|KeyStoreException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|NoSuchAlgorithmException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|UnrecoverableKeyException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|cert
operator|.
name|CertificateException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Locale
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Properties
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

begin_comment
comment|/**  * A basic KeyProvider that can resolve keys from a protected KeyStore file  * on the local filesystem. It is configured with a URI passed in as a String  * to init(). The URI should have the form:  *<p>  *<pre>    scheme://path?option1=value1&amp;option2=value2</pre>  *<p>  *<i>scheme</i> can be either "jks" or "jceks", specifying the file based  * providers shipped with every JRE. The latter is the certificate store for  * the SunJCE cryptography extension, or PKCS #12, and is capable of storing  * SecretKeys.  *<p>  *<i>path</i> is the location of the keystore in the filesystem namespace.  *<p>  * Options can be specified as query parameters.  *<p>  * If the store was created with a password, the password can be specified  * using the option 'password'.  *<p>  * For example:  *<p>  *<pre>    jceks:///var/tmp/example.ks?password=foobar</pre>  *<p>  * It is assumed that all keys in the store are protected with the same  * password.  *<p>  * Alternatively, a properties file can be specified containing passwords for  * keys in the keystore.  *<pre>    jceks:///var/tmp/example.ks?passwordFile=/var/tmp/example.pw</pre>  *<p>  * Subclasses for supporting KeyStores that are not file based can extend the  * protected methods of this class to specify the appropriate  * LoadStoreParameters.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
class|class
name|KeyStoreKeyProvider
implements|implements
name|KeyProvider
block|{
specifier|protected
name|KeyStore
name|store
decl_stmt|;
specifier|protected
name|char
index|[]
name|password
decl_stmt|;
comment|// can be null if no password
specifier|protected
name|Properties
name|passwordFile
decl_stmt|;
comment|// can be null if no file provided
specifier|protected
name|void
name|processParameter
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|value
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|name
operator|.
name|equalsIgnoreCase
argument_list|(
name|KeyProvider
operator|.
name|PASSWORD
argument_list|)
condition|)
block|{
name|password
operator|=
name|value
operator|.
name|toCharArray
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|name
operator|.
name|equalsIgnoreCase
argument_list|(
name|KeyProvider
operator|.
name|PASSWORDFILE
argument_list|)
condition|)
block|{
name|Properties
name|p
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
name|InputStream
name|in
init|=
operator|new
name|BufferedInputStream
argument_list|(
operator|new
name|FileInputStream
argument_list|(
operator|new
name|File
argument_list|(
name|value
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
try|try
block|{
name|p
operator|.
name|load
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|passwordFile
operator|=
name|p
expr_stmt|;
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
specifier|protected
name|void
name|processParameters
parameter_list|(
name|URI
name|uri
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|params
init|=
name|uri
operator|.
name|getQuery
argument_list|()
decl_stmt|;
if|if
condition|(
name|params
operator|==
literal|null
operator|||
name|params
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return;
block|}
do|do
block|{
name|int
name|nameStart
init|=
literal|0
decl_stmt|;
name|int
name|nameEnd
init|=
name|params
operator|.
name|indexOf
argument_list|(
literal|'='
argument_list|)
decl_stmt|;
if|if
condition|(
name|nameEnd
operator|==
operator|-
literal|1
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Invalid parameters: '"
operator|+
name|params
operator|+
literal|"'"
argument_list|)
throw|;
block|}
name|int
name|valueStart
init|=
name|nameEnd
operator|+
literal|1
decl_stmt|;
name|int
name|valueEnd
init|=
name|params
operator|.
name|indexOf
argument_list|(
literal|'&'
argument_list|)
decl_stmt|;
if|if
condition|(
name|valueEnd
operator|==
operator|-
literal|1
condition|)
block|{
name|valueEnd
operator|=
name|params
operator|.
name|length
argument_list|()
expr_stmt|;
block|}
name|String
name|name
init|=
name|URLDecoder
operator|.
name|decode
argument_list|(
name|params
operator|.
name|substring
argument_list|(
name|nameStart
argument_list|,
name|nameEnd
argument_list|)
argument_list|,
literal|"UTF-8"
argument_list|)
decl_stmt|;
name|String
name|value
init|=
name|URLDecoder
operator|.
name|decode
argument_list|(
name|params
operator|.
name|substring
argument_list|(
name|valueStart
argument_list|,
name|valueEnd
argument_list|)
argument_list|,
literal|"UTF-8"
argument_list|)
decl_stmt|;
name|processParameter
argument_list|(
name|name
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|params
operator|=
name|params
operator|.
name|substring
argument_list|(
name|valueEnd
argument_list|,
name|params
operator|.
name|length
argument_list|()
argument_list|)
expr_stmt|;
block|}
do|while
condition|(
operator|!
name|params
operator|.
name|isEmpty
argument_list|()
condition|)
do|;
block|}
specifier|protected
name|void
name|load
parameter_list|(
name|URI
name|uri
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|path
init|=
name|uri
operator|.
name|getPath
argument_list|()
decl_stmt|;
if|if
condition|(
name|path
operator|==
literal|null
operator|||
name|path
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"KeyProvider parameters should specify a path"
argument_list|)
throw|;
block|}
name|InputStream
name|is
init|=
operator|new
name|FileInputStream
argument_list|(
operator|new
name|File
argument_list|(
name|path
argument_list|)
argument_list|)
decl_stmt|;
try|try
block|{
name|store
operator|.
name|load
argument_list|(
name|is
argument_list|,
name|password
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NoSuchAlgorithmException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|CertificateException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
name|is
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|init
parameter_list|(
name|String
name|params
parameter_list|)
block|{
try|try
block|{
name|URI
name|uri
init|=
operator|new
name|URI
argument_list|(
name|params
argument_list|)
decl_stmt|;
name|String
name|storeType
init|=
name|uri
operator|.
name|getScheme
argument_list|()
decl_stmt|;
if|if
condition|(
name|storeType
operator|==
literal|null
operator|||
name|storeType
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"KeyProvider scheme should specify KeyStore type"
argument_list|)
throw|;
block|}
comment|// KeyStore expects instance type specifications in uppercase
name|store
operator|=
name|KeyStore
operator|.
name|getInstance
argument_list|(
name|storeType
operator|.
name|toUpperCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
argument_list|)
expr_stmt|;
name|processParameters
argument_list|(
name|uri
argument_list|)
expr_stmt|;
name|load
argument_list|(
name|uri
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|URISyntaxException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|KeyStoreException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
specifier|protected
name|char
index|[]
name|getAliasPassword
parameter_list|(
name|String
name|alias
parameter_list|)
block|{
if|if
condition|(
name|password
operator|!=
literal|null
condition|)
block|{
return|return
name|password
return|;
block|}
if|if
condition|(
name|passwordFile
operator|!=
literal|null
condition|)
block|{
name|String
name|p
init|=
name|passwordFile
operator|.
name|getProperty
argument_list|(
name|alias
argument_list|)
decl_stmt|;
if|if
condition|(
name|p
operator|!=
literal|null
condition|)
block|{
return|return
name|p
operator|.
name|toCharArray
argument_list|()
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|Key
name|getKey
parameter_list|(
name|String
name|alias
parameter_list|)
block|{
try|try
block|{
return|return
name|store
operator|.
name|getKey
argument_list|(
name|alias
argument_list|,
name|getAliasPassword
argument_list|(
name|alias
argument_list|)
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|UnrecoverableKeyException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|KeyStoreException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|NoSuchAlgorithmException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|Key
index|[]
name|getKeys
parameter_list|(
name|String
index|[]
name|aliases
parameter_list|)
block|{
name|Key
index|[]
name|result
init|=
operator|new
name|Key
index|[
name|aliases
operator|.
name|length
index|]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|aliases
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|result
index|[
name|i
index|]
operator|=
name|getKey
argument_list|(
name|aliases
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
block|}
end_class

end_unit

