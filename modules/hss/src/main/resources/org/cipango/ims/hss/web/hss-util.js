function setSubscriptionName(element)
{
    var subscriptionName = document.getElementById("subscriptionName");
    if (subscriptionName.value == null || subscriptionName.value == "")
        subscriptionName.value = element.value;
}

function setPrivateIdentity(element)
{
    var privateIdentity = document.getElementById("privateIdentity");
    var index = element.value.indexOf(":");
    if ((privateIdentity.value == null || privateIdentity.value == "") && index != -1)
        privateIdentity.value = element.value.substring(index + 1);
    
}